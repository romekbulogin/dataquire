package ru.dataquire.databasemanager.service

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.jooq.impl.DSL
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.databasemanager.dto.UserCredentials
import ru.dataquire.databasemanager.entity.DatabaseEntity
import ru.dataquire.databasemanager.feign.InstanceKeeperClient
import ru.dataquire.databasemanager.feign.request.FindInstance
import ru.dataquire.databasemanager.feign.request.InstanceEntity
import ru.dataquire.databasemanager.repository.DatabaseRepository
import ru.dataquire.databasemanager.repository.UserRepository
import ru.dataquire.databasemanager.request.ChangeCredentialsRequest
import ru.dataquire.databasemanager.request.DatabaseRequest
import ru.dataquire.databasemanager.request.DeleteDatabaseRequest
import ru.dataquire.databasemanager.request.OwnDatabaseRequest
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.crypto.Cipher
import kotlin.math.log


@Service
class DatabaseService(
    private val instanceKeeperClient: InstanceKeeperClient,
    private val userRepository: UserRepository,
    private val databaseRepository: DatabaseRepository,
    private val jwtService: JwtService,
    private val encryptCipher: Cipher,
    private val decryptCipher: Cipher
) {
    private val logger = KotlinLogging.logger { }

    private val createUserQuery = "CREATE USER usertag WITH PASSWORD 'passtag';"
    private fun findDriver(driverName: String): InstanceEntity? {
        return try {
            instanceKeeperClient.findInstanceByDbms(FindInstance(driverName))
        } catch (ex: Exception) {
            logger.error(ex.message)
            throw RuntimeException("DBMS url not found")
        }
    }

    private fun convertCreateUserQuery(dbms: String): String {
        return when (dbms) {
            "PostgreSQL" -> createUserQuery
            "MySQL", "Oracle" -> {
                createUserQuery.replace("WITH PASSWORD", "IDENTIFIED BY")
                    .replace("'", "\"")
            }

            "MSSQL" -> {
                createUserQuery.replace("CREATE USER", "CREATE LOGIN")
                    .replace("WITH PASSWORD", "WITH PASSWORD =")
            }

            else -> {
                throw Exception("$dbms is not found")
            }
        }
    }

    private fun convertUpdateUsernameQuery(dbms: String): String {
        return when (dbms) {
            "PostgreSQL" -> "ALTER USER oldusername RENAME TO newusername;"
            "MySQL" -> "RENAME USER oldusername TO newusername;"
            "Oracle" -> "ALTER USER oldusername RENAME TO newusername;"
            "MSSQL" -> "ALTER LOGIN oldusername WITH NAME = newusername;"
            else -> {
                throw Exception("$dbms is not found")
            }
        }
    }

    private fun convertUpdatePasswordQuery(dbms: String): String {
        return when (dbms) {
            "PostgreSQL" -> "ALTER USER usertag WITH PASSWORD 'passtag';"
            "MySQL" -> "ALTER USER 'usertag'@'%' IDENTIFIED BY 'passtag';";
            "Oracle" -> "ALTER USER usertag IDENTIFIED BY passtag;"
            "MSSQL" -> "ALTER LOGIN usertag WITH PASSWORD = 'passtag';"
            else -> {
                throw Exception("$dbms is not found")
            }
        }
    }


    private fun convertDeleteUserQuery(dbms: String): String {
        return when (dbms) {
            "PostgreSQL" -> "DROP USER usertag;"
            "MySQL" -> "DROP USER 'usertag'@'%';";
            "Oracle" -> "DROP USER usertag;"
            "MSSQL" -> "DROP LOGIN usertag;"
            else -> {
                throw Exception("$dbms is not found")
            }
        }
    }

    private fun convertDatabaseGrant(dbms: String, systemName: String, username: String): String {
        return when (dbms) {
            "PostgreSQL" -> "ALTER DATABASE $systemName OWNER TO $username"
            "MySQL" -> "GRANT ALL PRIVILEGES ON ${systemName}.* TO '${username}'@'%';";
            "Oracle" -> "DROP USER usertag;"
            "MSSQL" -> "ALTER AUTHORIZATION ON DATABASE::${systemName} TO $username"
            else -> {
                throw Exception("$dbms is not found")
            }
        }
    }

    fun createDatabase(request: DatabaseRequest, token: String): ResponseEntity<Map<String, String>> {
        val systemName =
            RandomStringUtils.random(10, true, false).lowercase(Locale.getDefault()) + RandomStringUtils.random(
                30,
                true,
                true
            ).lowercase(Locale.getDefault())

        val targetDatabase = findDriver(request.dbms!!)

        val user = createUser(targetDatabase!!)
        val passwordBytes = user.password?.toByteArray(Charsets.UTF_8)
        return try {
            DriverManager.getConnection(targetDatabase.url, targetDatabase.username, targetDatabase.password)
                .use { connection ->
                    DSL.using(connection).createDatabase(systemName).execute()
                    connection.createStatement()
                        .executeUpdate(convertDatabaseGrant(targetDatabase.dbms!!, systemName, user.username!!))
                }
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val database = DatabaseEntity().apply {
                this.dbms = request.dbms
                this.systemName = systemName
                this.databaseName = request.database
                this.userEntity = currentUser
                this.login = user.username
                this.url = targetDatabase.url + systemName
                this.isImported = false
                this.passwordDbms =
                    Base64.getEncoder()
                        .encodeToString(encryptCipher.doFinal(passwordBytes))
            }
            databaseRepository.save(database)
            currentUser.addDatabase(database)
            userRepository.save(currentUser)

            logger.info("${request.dbms}: ${request.database} created successfully")
            val response = ResponseEntity(
                mapOf(
                    "status" to "Database ${request.database} successfully created"
                ), HttpStatus.OK
            )
            response
        } catch (ex: Exception) {
            logger.error("Database creation error: ${request.database}. Exception: ${ex.message}")
            DriverManager.getConnection(targetDatabase.url, targetDatabase.username, targetDatabase.password)
                .use { connection ->
                    DSL.using(connection).dropDatabase(systemName).execute()
                    connection?.createStatement()
                        ?.execute(convertDeleteUserQuery(request.dbms!!).replace("usertag", user.username!!))
                }
            ResponseEntity(
                mapOf(
                    "error" to "Database creation error: ${request.database}",
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun addYourOwnDatabase(request: OwnDatabaseRequest, token: String): ResponseEntity<Map<String, String>> {
        return try {
            val systemName =
                RandomStringUtils.random(10, true, false).lowercase(Locale.getDefault()) + RandomStringUtils.random(
                    30,
                    true,
                    true
                ).lowercase(Locale.getDefault())

            val targetDatabase = findDriver(request.dbms!!)
            request.url = replaceIpAddress(targetDatabase?.url!!, request.url!!, request.database!!)
            logger.info(request.toString())
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val database = DatabaseEntity().apply {
                this.dbms = request.dbms
                this.systemName = systemName
                this.databaseName = request.database
                this.userEntity = currentUser
                this.login = request.login
                this.url = request.url
                this.isImported = true
                this.passwordDbms =
                    Base64.getEncoder()
                        .encodeToString(encryptCipher.doFinal(request.password?.toByteArray(Charsets.UTF_8)))
            }
            databaseRepository.save(database)
            currentUser.addDatabase(database)
            userRepository.save(currentUser)
            ResponseEntity(
                mapOf(
                    "status" to "Database ${request.database} successfully added"
                ), HttpStatus.OK
            )
        } catch (ex: Exception) {
            logger.error("Database added error: ${request.database}. Exception: ${ex.message}")
            ResponseEntity(
                mapOf(
                    "error" to "Database added error: ${request.database}",
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun deleteDatabase(request: DeleteDatabaseRequest, token: String): ResponseEntity<Map<String, Any>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndDbmsAndSystemNameAndDatabaseName(
                    currentUser,
                    request.dbms.toString(),
                    request.systemName.toString(),
                    request.database.toString()
                )
            if (currentDatabase.isImported == true) {
                val targetDatabase = findDriver(request.dbms!!)
                DriverManager.getConnection(
                    targetDatabase?.url, targetDatabase?.username, targetDatabase?.password
                ).use { connection ->
                    DSL.using(connection).dropDatabase(request.systemName).execute()
                    connection?.createStatement()
                        ?.execute(convertDeleteUserQuery(request.dbms!!).replace("usertag", currentDatabase.login!!))
                    logger.info("Database: ${request.database} deleted successfully")
                }
            }
            ResponseEntity(
                mapOf("response" to "Database: ${request.database} deleted successfully"),
                HttpStatus.OK
            )
        } catch (ex: Exception) {
            logger.error("Database deletion error: ${request.database}. Exception: ${ex.message}")
            ResponseEntity(
                mapOf(
                    "error" to "Database deletion error: ${request.database}"
                ), HttpStatus.BAD_REQUEST
            )
        }

    }

    fun deleteYourOwnDatabase(request: DeleteDatabaseRequest, token: String): ResponseEntity<Map<String, Any>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByDatabaseNameAndDbmsAndSystemName(
                    request.database.toString(),
                    request.dbms.toString(),
                    request.systemName.toString()
                )
            currentUser.deleteDatabase(currentDatabase)
            databaseRepository.delete(currentDatabase)
            logger.info("Database: ${request.database} deleted successfully")
            ResponseEntity(
                mapOf("response" to "Database: ${request.database} deleted successfully"),
                HttpStatus.OK
            )
        } catch (ex: Exception) {
            logger.error("Database deletion error: ${request.database}. Exception: ${ex.message}")
            ResponseEntity(
                mapOf(
                    "error" to "Database deletion error: ${request.database}"
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun findAllDatabases(token: String): ResponseEntity<Any> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            ResponseEntity(
                databaseRepository.findAllByUserEntity(currentUser), HttpStatus.OK
            )
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(
                mapOf(
                    "error" to ex.message.toString()
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun findDatabase(token: String, systemName: String): ResponseEntity<Any> {
        return try {

            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase = databaseRepository.findDatabaseEntityByUserEntityAndSystemName(
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

            ResponseEntity(
                currentDatabase, HttpStatus.OK
            )
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(
                mapOf(
                    "error" to ex.message.toString()
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun findDatabaseInDBMS(token: String, dbms: String): ResponseEntity<Any> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            ResponseEntity(
                databaseRepository.findAllByUserEntityAndDbms(
                    currentUser,
                    dbms
                ), HttpStatus.OK
            )
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(
                mapOf(
                    "error" to ex.message.toString()
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun updateCredentials(request: ChangeCredentialsRequest, token: String): ResponseEntity<Map<String, Any>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndDbmsAndSystemNameAndDatabaseName(
                    currentUser,
                    request.dbms.toString(),
                    request.systemName.toString(),
                    request.database.toString()
                )
            val instance = findDriver(currentDatabase.dbms!!)
            DriverManager.getConnection(
                instance?.url, instance?.username, instance?.password
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
                ResponseEntity(null, HttpStatus.OK)
            }
        } catch (ex: Exception) {
            logger.error("Change user credentials error: ${request.database}. Exception: ${ex.message}")
            ResponseEntity(
                mapOf(
                    "error" to "Change user credentials error: ${request.database}"
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun getDbmsList() = ResponseEntity(listOf("PostgreSQL", "MySQL", "MSSQL", "Oracle"), HttpStatus.OK)

    private fun createUser(instance: InstanceEntity): UserCredentials {
        return try {
            DriverManager.getConnection(instance.url, instance.username, instance.password).use { connection ->
                val password = RandomStringUtils.random(30, true, true).lowercase()
                val login = RandomStringUtils.random(10, true, false).lowercase()

                connection.createStatement().executeUpdate(
                    convertCreateUserQuery(instance.dbms!!).replace("usertag", login)
                        .replace("passtag", password)
                )
                UserCredentials(login, password)
            }
        } catch (ex: Exception) {
            logger.error(ex.message)
            throw ex
        }
    }

    fun getTablesOfDatabase(token: String, systemName: String): ResponseEntity<Any> {

        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(currentUser, systemName)
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
            ResponseEntity(tables, HttpStatus.OK)
        } catch (ex: Exception) {
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun getDatabaseStructure(token: String, systemName: String, table: String): ResponseEntity<Any> {
        return try {
            val columns = mutableMapOf<String, String>()
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(currentUser, systemName)
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
            ResponseEntity(columns, HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(mapOf("status" to "Не удалось получить структуру базы данных"), HttpStatus.BAD_REQUEST)
        }
    }

    private fun replaceIpAddress(connectionString: String, newIpAddress: String, databaseName: String): String? {
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