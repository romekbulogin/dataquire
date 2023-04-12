package ru.dataquire.databasemanager.service

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.databasemanager.entity.DatabaseEntity
import ru.dataquire.databasemanager.feign.InstancesManagerClient
import ru.dataquire.databasemanager.feign.request.FindInstance
import ru.dataquire.databasemanager.feign.request.InstanceEntity
import ru.dataquire.databasemanager.repository.DatabaseRepository
import ru.dataquire.databasemanager.repository.UserRepository
import ru.dataquire.databasemanager.request.DatabaseRequest
import ru.dataquire.databasemanager.request.DeleteDatabaseRequest
import ru.dataquire.databasemanager.response.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.crypto.Cipher


@Service
class DatabaseService(
    private val instancesManagerClient: InstancesManagerClient,
    private val userRepository: UserRepository,
    private val databaseRepository: DatabaseRepository,
    private val jwtService: JwtService,
    private val encryptCipher: Cipher
) {
    private val logger = KotlinLogging.logger { }
    fun findDriver(driverName: String): InstanceEntity? {
        return try {
            instancesManagerClient.findInstanceByDbms(FindInstance(driverName))
        } catch (ex: Exception) {
            logger.error(ex.message)
            throw RuntimeException("DBMS url not found")
        }
    }

    fun createDatabase(request: DatabaseRequest, token: String): ResponseEntity<Map<String, String?>> {
        var connection: Connection? = null
        val systemName = RandomStringUtils.random(40, true, true).lowercase(Locale.getDefault())
        return try {
            val targetDatabase = findDriver(request.dbms!!)
            val user = createUser(targetDatabase!!)
            connection =
                DriverManager.getConnection(targetDatabase.url, targetDatabase.username, targetDatabase.password)
            connection?.createStatement()
                ?.executeUpdate("create database \"$systemName\"; ALTER DATABASE \"$systemName\" OWNER TO ${user.username};")

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
                    "url" to "${connection.metaData?.url}$systemName",
                    "username" to user.username,
                    "password" to user.password

                ), HttpStatus.OK
            )
            connection!!.endRequest()
            connection.close()
            return response
        } catch (ex: Exception) {
            logger.error("Database creation error: ${request.database}. Exception: ${ex.message}")
            connection?.createStatement()?.execute("drop database \"$systemName\"")
            return ResponseEntity(
                mapOf(
                    "error" to "Database creation error: ${request.database}",
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun deleteDatabase(request: DeleteDatabaseRequest, token: String): ResponseEntity<Any> {
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
            connection?.createStatement()?.execute("drop database \"${currentDatabase.systemName}\"")
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
            ResponseEntity(FailedDeletedDatabase().apply {
                error = "Database deletion error: ${request.database}"
                exception = ex.message
            }, HttpStatus.BAD_REQUEST)
        }
    }

    fun findAllDatabases(token: String): ResponseEntity<Map<String, Any>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            ResponseEntity(
                mapOf(
                    "databases" to databaseRepository.findAllByUserEntity(currentUser)
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

    fun findDatabase(token: String, systemName: String): ResponseEntity<Map<String, Any>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            ResponseEntity(
                mapOf(
                    "databases" to databaseRepository.findDatabaseEntityByUserEntityAndSystemName(
                        currentUser,
                        systemName
                    )
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

    private fun createUser(instance: InstanceEntity): CreateUserResponse {
        return try {
            val connection = DriverManager.getConnection(instance.url, instance.username, instance.password)

            val password = RandomStringUtils.random(30, true, true).lowercase(Locale.getDefault())
            val login = RandomStringUtils.random(10, true, false).lowercase(Locale.getDefault())

            connection.createStatement().execute(
                instance.sqlCreateUser?.replace("usertag", login)
                    ?.replace("passtag", password)
            )
            connection.close()
            CreateUserResponse(login, password)
        } catch (ex: SQLException) {
            logger.error(ex.message)
            throw ex
        }
    }
}