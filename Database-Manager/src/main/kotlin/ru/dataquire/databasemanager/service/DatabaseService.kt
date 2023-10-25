package ru.dataquire.databasemanager.service

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.jooq.impl.DSL
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.databasemanager.dto.UserCredentials
import ru.dataquire.databasemanager.entity.DatabaseEntity
import ru.dataquire.databasemanager.feign.InstanceKeeperClient
import ru.dataquire.databasemanager.request.InstanceEntity
import ru.dataquire.databasemanager.repository.DatabaseRepository
import ru.dataquire.databasemanager.repository.OwnerRepository
import ru.dataquire.databasemanager.request.ChangeCredentialsRequest
import ru.dataquire.databasemanager.request.DatabaseRequest
import ru.dataquire.databasemanager.request.DeleteDatabaseRequest
import ru.dataquire.databasemanager.request.OwnDatabaseRequest
import ru.dataquire.databasemanager.extension.QueryForUser.Companion.convertCreateUserQuery
import ru.dataquire.databasemanager.extension.QueryForUser.Companion.convertDatabaseGrant
import ru.dataquire.databasemanager.extension.QueryForUser.Companion.convertDeleteUserQuery
import ru.dataquire.databasemanager.extension.QueryForUser.Companion.convertUpdatePasswordQuery
import ru.dataquire.databasemanager.extension.QueryForUser.Companion.convertUpdateUsernameQuery
import java.sql.DriverManager
import java.util.*
import javax.crypto.Cipher


@Service
class DatabaseService(
    private val instanceKeeperClient: InstanceKeeperClient,
    private val ownerRepository: OwnerRepository,
    private val databaseRepository: DatabaseRepository,
    private val jwtService: JwtService,
    private val encryptCipher: Cipher,
    private val decryptCipher: Cipher
) {
    private val logger = KotlinLogging.logger { }
    fun createDatabase(request: DatabaseRequest, token: String): ResponseEntity<Map<String, String>> {
        val targetDatabase = findInstance(request.dbms)
        val systemName = generateSystemName()
        val user = createUser(targetDatabase)
        val passwordBytes = user.password.toByteArray(Charsets.UTF_8)

        return try {
            DriverManager.getConnection(targetDatabase.url, targetDatabase.username, targetDatabase.password)
                .use { connection ->
                    DSL.using(connection).createDatabase(systemName).execute()
                    connection.createStatement()
                        .executeUpdate(
                            convertDatabaseGrant(targetDatabase.dbms, systemName, user.username)
                        )
                }
            val currentUser = getOwnerEntity(token)
            val database = DatabaseEntity().apply {
                this.dbms = request.dbms
                this.systemName = systemName
                this.databaseName = request.database
                this.ownerEntity = currentUser
                this.login = user.username
                this.url = targetDatabase.url + systemName
                this.isImported = false
                this.passwordDbms =
                    Base64.getEncoder()
                        .encodeToString(
                            encryptCipher.doFinal(passwordBytes)
                        )
            }
            databaseRepository.save(database)
            currentUser.addDatabase(database)
            ownerRepository.save(currentUser)

            logger.info("${request.dbms}: ${request.database} created successfully")
            ResponseEntity.ok().body(
                mapOf(
                    "status" to "Database ${request.database} successfully created"
                )
            )
        } catch (ex: Exception) {
            logger.error("Database creation error: ${request.database}. Exception: ${ex.message}")
            DriverManager.getConnection(targetDatabase.url, targetDatabase.username, targetDatabase.password)
                .use { connection ->
                    DSL.using(connection).dropDatabase(systemName).execute()
                    connection?.createStatement()
                        ?.execute(convertDeleteUserQuery(request.dbms).replace("usertag", user.username))
                }
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Database creation error: ${request.database}",
                )
            )
        }
    }

    fun addYourOwnDatabase(request: OwnDatabaseRequest, token: String) = try {
        val systemName = generateSystemName()

        val targetDatabase = findInstance(request.dbms)
        request.url = replaceIpAddress(targetDatabase.url, request.url, request.database)
        logger.info(request.toString())
        val currentUser = getOwnerEntity(token)
        val database = DatabaseEntity().apply {
            this.dbms = request.dbms
            this.systemName = systemName
            this.databaseName = request.database
            this.ownerEntity = currentUser
            this.login = request.login
            this.url = request.url
            this.isImported = true
            this.passwordDbms =
                Base64.getEncoder()
                    .encodeToString(encryptCipher.doFinal(request.password.toByteArray(Charsets.UTF_8)))
        }
        databaseRepository.save(database)
        currentUser.addDatabase(database)
        ownerRepository.save(currentUser)
        ResponseEntity.ok().body(
            mapOf(
                "status" to "Database ${request.database} successfully added"
            )
        )
    } catch (ex: Exception) {
        logger.error("Database added error: ${request.database}. Exception: ${ex.message}")
        ResponseEntity.badRequest().body(
            mapOf(
                "error" to "Database added error: ${request.database}",
            )
        )
    }

    fun deleteDatabase(request: DeleteDatabaseRequest, token: String) = try {
        val currentUser = getOwnerEntity(token)
        val currentDatabase =
            databaseRepository.findByOwnerEntityAndDbmsAndSystemNameAndDatabaseName(
                currentUser,
                request.dbms,
                request.systemName,
                request.database
            )
        if (currentDatabase.isImported == true) {
            val targetDatabase = findInstance(request.dbms)
            DriverManager.getConnection(
                targetDatabase.url, targetDatabase.username, targetDatabase.password
            ).use { connection ->
                DSL.using(connection).dropDatabase(request.systemName).execute()
                connection?.createStatement()
                    ?.execute(convertDeleteUserQuery(request.dbms).replace("usertag", currentDatabase.login!!))
                logger.info("Database: ${request.database} deleted successfully")
            }
        }
        ResponseEntity.ok().body(
            mapOf(
                "status" to "Database: ${request.database} deleted successfully"
            )
        )
    } catch (ex: Exception) {
        logger.error("Database deletion error: ${request.database}. Exception: ${ex.message}")
        ResponseEntity.badRequest().body(
            mapOf(
                "error" to "Database deletion error: ${request.database}"
            )
        )
    }

    fun deleteYourOwnDatabase(request: DeleteDatabaseRequest, token: String) = try {
        val currentUser = getOwnerEntity(token)
        val currentDatabase =
            databaseRepository.findByDatabaseNameAndDbmsAndSystemName(
                request.database,
                request.dbms,
                request.systemName
            )
        currentUser.deleteDatabase(currentDatabase)
        databaseRepository.delete(currentDatabase)
        logger.info("Database: ${request.database} deleted successfully")
        ResponseEntity.ok().body(
            mapOf(
                "response" to "Database: ${request.database} deleted successfully"
            )
        )
    } catch (ex: Exception) {
        logger.error("Database deletion error: ${request.database}. Exception: ${ex.message}")
        ResponseEntity.badRequest().body(
            mapOf(
                "error" to "Database deletion error: ${request.database}"
            )
        )
    }

    fun findAllDatabases(token: String) = try {
        val currentUser = getOwnerEntity(token)
        val ownerDatabases = databaseRepository.findAllByOwnerEntity(currentUser)
        ResponseEntity.ok().body(ownerDatabases)
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity.badRequest().body(
            mapOf(
                "error" to ex.message.toString()
            )
        )
    }

    fun findDatabase(token: String, systemName: String) = try {
        val currentUser = getOwnerEntity(token)
        val currentDatabase = databaseRepository.findByOwnerEntityAndSystemName(
            currentUser,
            systemName
        )
        currentDatabase.apply {
            passwordDbms = String(
                decryptCipher.doFinal(
                    Base64.getDecoder().decode(currentDatabase.passwordDbms)
                )
            )
        }
        ResponseEntity.ok().body(currentDatabase)
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity.badRequest().body(
            mapOf(
                "error" to "Database not found"
            )
        )
    }

    fun findDatabaseInDBMS(token: String, dbms: String) = try {
        val currentUser = getOwnerEntity(token)
        val ownerDatabases = databaseRepository.findAllByOwnerEntityAndDbms(
            currentUser,
            dbms
        )
        ResponseEntity.ok().body(ownerDatabases)
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity.badRequest().body(
            mapOf(
                "error" to "Databases not found"
            )
        )
    }

    fun updateCredentials(request: ChangeCredentialsRequest, token: String) = try {
        val currentUser = getOwnerEntity(token)
        val currentDatabase =
            databaseRepository.findByOwnerEntityAndDbmsAndSystemNameAndDatabaseName(
                currentUser,
                request.dbms,
                request.systemName,
                request.database
            )
        val instance = findInstance(currentDatabase.dbms!!)
        DriverManager.getConnection(
            instance.url, instance.username, instance.password
        ).use { connection ->
            val password = RandomStringUtils.random(30, true, true).lowercase(Locale.getDefault())
            val login = RandomStringUtils.random(10, true, false).lowercase(Locale.getDefault())
            connection.createStatement().execute(
                convertUpdateUsernameQuery(currentDatabase.dbms!!).replace("oldusername", currentDatabase.login!!)
                    .replace("newusername", login)
            )
            connection.createStatement().execute(
                convertUpdatePasswordQuery(currentDatabase.dbms!!).replace("usertag", login)
                    .replace("passtag", password)
            )
            connection?.endRequest()
            connection.close()
            with(currentDatabase) {
                this.login = login
                this.passwordDbms = Base64.getEncoder()
                    .encodeToString(encryptCipher.doFinal(password.toByteArray(Charsets.UTF_8)))
            }
            databaseRepository.save(currentDatabase)
        }
        ResponseEntity.ok().body(
            mapOf("status" to "Update credentials successfully")
        )
    } catch (ex: Exception) {
        logger.error("Change user credentials error: ${request.database}. Exception: ${ex.message}")
        ResponseEntity.badRequest().body(
            mapOf(
                "error" to "Change user credentials error: ${request.database}"
            )
        )
    }

    fun getTablesOfDatabase(token: String, systemName: String) = try {
        val currentUser = getOwnerEntity(token)
        val currentDatabase =
            databaseRepository.findByOwnerEntityAndSystemName(currentUser, systemName)
        val tables = mutableListOf<String>()
        DriverManager.getConnection(
            currentDatabase.url,
            currentDatabase.login,
            String(
                decryptCipher.doFinal(
                    Base64.getDecoder().decode(currentDatabase.passwordDbms)
                )
            )
        ).use { connection ->
            val rs = connection.metaData.getTables(null, null, "%", arrayOf("TABLE"))
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"))
            }
            rs.close()
        }
        ResponseEntity.ok().body(tables)
    } catch (ex: Exception) {
        ResponseEntity.badRequest().body(
            mapOf("error" to "Failed to get database tables")
        )
    }

    fun getDatabaseStructure(token: String, systemName: String, table: String) = try {
        val columns = mutableMapOf<String, String>()
        val currentUser = getOwnerEntity(token)
        val currentDatabase =
            databaseRepository.findByOwnerEntityAndSystemName(currentUser, systemName)
        DriverManager.getConnection(
            currentDatabase.url,
            currentDatabase.login,
            String(
                decryptCipher.doFinal(
                    Base64.getDecoder().decode(currentDatabase.passwordDbms)
                )
            )
        ).use { connection ->
            val resultSetColumns = connection.metaData.getColumns(null, null, table, null)

            while (resultSetColumns.next()) {
                columns[resultSetColumns.getString("COLUMN_NAME")] = resultSetColumns.getString("TYPE_NAME")
            }
            resultSetColumns.close()
        }
        ResponseEntity.ok().body(columns)
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity.badRequest().body(
            mapOf(
                "status" to "Failed to get database structure"
            )
        )
    }

    private fun generateSystemName() = RandomStringUtils
        .random(10, true, false)
        .lowercase(Locale.getDefault()) + RandomStringUtils.random(
        30,
        true,
        true
    ).lowercase(Locale.getDefault())

    private fun findInstance(dbms: String) = instanceKeeperClient.findInstanceByDbms(dbms)
    private fun createUser(instance: InstanceEntity): UserCredentials {
        return try {
            DriverManager.getConnection(
                instance.url,
                instance.username,
                instance.password
            ).use { connection ->
                val password = RandomStringUtils.random(30, true, true).lowercase()
                val login = RandomStringUtils.random(10, true, false).lowercase()

                connection.createStatement().executeUpdate(
                    convertCreateUserQuery(instance.dbms)
                        .replace("usertag", login)
                        .replace("passtag", password)
                )
                UserCredentials(login, password)
            }
        } catch (ex: Exception) {
            logger.error(ex.message)
            throw ex
        }
    }

    private fun getOwnerEntity(token: String) =
        ownerRepository.findByEmail(
            jwtService.extractUsername(
                token.substring(7)
            )
        )

    private fun replaceIpAddress(connectionString: String, newIpAddress: String, databaseName: String): String {
        val splitAddress = connectionString.split("//").toMutableList()
        val stringBuilder = StringBuilder()
        stringBuilder.append(splitAddress[0]).append("//")


        var address = splitAddress[1].split("/")
        if (address.size == 2) {
            stringBuilder.append(newIpAddress).append("/").append(databaseName)
        } else {
            address = splitAddress[1].split(";")
            stringBuilder.append(newIpAddress).append(";").append(address[1]).append(databaseName)
        }

        return stringBuilder.toString()
    }
}