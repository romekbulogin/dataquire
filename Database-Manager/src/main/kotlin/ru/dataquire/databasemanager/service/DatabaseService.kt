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
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.crypto.Cipher


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
            "MySQL" -> "ALTER USER 'usertag'@'localhost' IDENTIFIED BY 'passtag';";
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
            "MySQL" -> "DROP USER usertag@'localhost';";
            "Oracle" -> "DROP USER usertag;"
            "MSSQL" -> "DROP LOGIN usertag;"
            else -> {
                throw Exception("$dbms is not found")
            }
        }
    }

    fun createDatabase(request: DatabaseRequest, token: String): ResponseEntity<Map<String, String>> {
        var connection: Connection? = null
        val systemName = RandomStringUtils.random(40, true, true).lowercase(Locale.getDefault())
        return try {
            val targetDatabase = findDriver(request.dbms!!)
            val user = createUser(targetDatabase!!)
            connection =
                DriverManager.getConnection(targetDatabase.url, targetDatabase.username, targetDatabase.password)
            connection?.createStatement()
                ?.executeUpdate("create database $systemName; ALTER DATABASE $systemName OWNER TO ${user.username};")

            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val database = DatabaseEntity().apply {
                this.dbms = request.dbms
                this.systemName = systemName
                this.databaseName = request.database
                this.userEntity = currentUser
                this.login = user.username
                this.passwordDbms =
                    Base64.getEncoder()
                        .encodeToString(encryptCipher.doFinal(user.password?.toByteArray(Charsets.UTF_8)))
            }
            databaseRepository.save(database)
            currentUser.addDatabase(database)
            userRepository.save(currentUser)

            logger.info("${connection?.metaData?.databaseProductName}: ${request.database} created successfully")

            val response = ResponseEntity(
                mapOf(
                    "status" to "Database ${request.database} successfully created"
                ), HttpStatus.OK
            )
            connection!!.endRequest()
            connection.close()
            response
        } catch (ex: Exception) {
            logger.error("Database creation error: ${request.database}. Exception: ${ex.message}")
            connection?.createStatement()?.execute("drop database $systemName")
            ResponseEntity(
                mapOf(
                    "error" to "Database creation error: ${request.database}",
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun deleteDatabase(request: DeleteDatabaseRequest, token: String): ResponseEntity<Map<String, Any>> {
        return try {
            val database = findDriver(request.dbms.toString())
            val connection = DriverManager.getConnection(database?.url, database?.username, database?.password)
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByDatabaseNameAndDbmsAndSystemName(
                    request.database.toString(),
                    request.dbms.toString(),
                    request.systemName.toString()
                )
            connection?.createStatement()?.execute("drop database ${currentDatabase.systemName}")
            connection?.createStatement()?.execute(
                convertDeleteUserQuery(database?.dbms!!).replace(
                    "usertag",
                    currentDatabase.login!!
                )
            )
            currentUser.deleteDatabase(currentDatabase)
            databaseRepository.delete(currentDatabase)
            logger.info("Database: ${request.database} deleted successfully")

            connection?.close()
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
            val instance = findDriver(request.dbms!!)
            val connection = DriverManager.getConnection(instance?.url, instance?.username, instance?.password)
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndDbmsAndSystemNameAndDatabaseName(
                    currentUser,
                    request.dbms.toString(),
                    request.systemName.toString(),
                    request.database.toString()
                )
            val password = RandomStringUtils.random(30, true, true).lowercase(Locale.getDefault())
            val login = RandomStringUtils.random(10, true, false).lowercase(Locale.getDefault())
            connection.createStatement().execute(
                convertUpdateUsernameQuery(instance?.dbms!!).replace("oldusername", currentDatabase.login!!)
                    .replace("newusername", login)
            )
            connection.createStatement().execute(
                convertUpdatePasswordQuery(instance.dbms!!).replace("usertag", login)
                    .replace("passtag", password)
            )
            connection.close()
            with(currentDatabase) {
                this.login = login
                this.passwordDbms = Base64.getEncoder()
                    .encodeToString(encryptCipher.doFinal(password.toByteArray(Charsets.UTF_8)))
            }
            databaseRepository.save(currentDatabase)
            return ResponseEntity(null, HttpStatus.OK)
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
            val connection = DriverManager.getConnection(instance.url, instance.username, instance.password)

            val password = RandomStringUtils.random(30, true, true).lowercase(Locale.getDefault())
            val login = RandomStringUtils.random(10, true, false).lowercase(Locale.getDefault())

            connection.createStatement().execute(
                convertCreateUserQuery(instance.dbms!!).replace("usertag", login)
                    .replace("passtag", password)
            )
            connection.close()
            UserCredentials(login, password)
        } catch (ex: SQLException) {
            logger.error(ex.message)
            throw ex
        }
    }

    fun getTablesOfDatabase(token: String, systemName: String): ResponseEntity<Any> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(currentUser, systemName)
            val instance = findDriver(currentDatabase.dbms.toString())
            val connection = DriverManager.getConnection(
                "${instance?.url}${currentDatabase.systemName}",
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            )
            val tables = mutableListOf<String>()

            val rs = connection.metaData.getTables(null, null, "%", arrayOf("TABLE"))
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"))
            }
            rs.close()
            connection.close()
            ResponseEntity(tables, HttpStatus.OK)
        } catch (ex: Exception) {
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }
}