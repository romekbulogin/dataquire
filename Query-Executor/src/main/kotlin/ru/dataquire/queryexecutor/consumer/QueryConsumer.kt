package ru.dataquire.queryexecutor.consumer

import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import ru.dataquire.queryexecutor.dto.UserCredentials
import ru.dataquire.queryexecutor.feign.InstanceKeeperClient
import ru.dataquire.queryexecutor.request.QueryRequest
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.crypto.Cipher

@Service
class QueryConsumer(
    private val mainDatabaseInstance: DriverManagerDataSource,
    private val decryptCipher: Cipher
) {
    private val logger = KotlinLogging.logger { }
    fun findTargetUrl(dbms: String, email: String, database: String): String? {
        return try {
            var url: String? = null
            val connection = mainDatabaseInstance.connection
            val statement =
                connection.prepareStatement("select url from _databases inner join _user u on u.id = _databases.user_entity_id where email = ? and system_name = ?")
            statement.setString(1, email)
            statement.setString(2, database)
            val resultSet = statement.executeQuery()
            while (resultSet?.next() == true) {
                for (i in 1..resultSet.metaData.columnCount) {
                    url = resultSet.getString("url")
                }
            }
            resultSet.close()
            connection.close()
            url
        } catch (ex: Exception) {
            logger.error(ex.message)
            null
        }
    }
    @RabbitListener(queues = ["\${spring.rabbitmq.consumer.request.queue}"], returnExceptions = "true")
    fun queryHandler(message: Message<String>): Any? {
        return try {
            logger.info(message.toString())
            val request = QueryRequest().apply {
                sql = message.payload
                database = message.headers["database"].toString()
                dbms = message.headers["dbms"].toString()
                login = message.headers["login"].toString()
            }
            return executeQuery(request)
        } catch (ex: Exception) {
            logger.error(ex.message)
            mapOf("error" to ex.message.toString())
        }
    }
    fun executeQuery(request: QueryRequest): Any? {
        try {
            logger.info("Request execute: $request")
            val userCredentials = getUserCredentials(request.login, request.database)
            val url = findTargetUrl(request.dbms, request.login, request.database)
            logger.info(userCredentials.toString())
            val result = mutableListOf<MutableMap<String, Any?>>()
            var map = mutableMapOf<String, Any?>()
            DriverManager.getConnection(
                url,
                userCredentials?.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(userCredentials?.password)
                    )
                )
            ).use { connection ->
                logger.debug("Current connection: $url")
                val resultSet = connection.createStatement()?.executeQuery(request.sql)
                while (resultSet?.next() == true) {
                    for (i in 1..resultSet.metaData.columnCount) {
                        map[resultSet.metaData.getColumnName(i)] = resultSet.getObject(i)
                    }
                    result.add(map)
                    map = mutableMapOf()
                }
                resultSet?.close()
            }

            return result
        } catch (ex: SQLException) {
            logger.error(ex.message)
            throw ex
        }
    }
    private fun getUserCredentials(username: String, database: String): UserCredentials? {
        return try {
            val userCredentials = UserCredentials()
            mainDatabaseInstance.connection.use { connection ->
                val statement =
                    connection.prepareStatement("select login,password_dbms from _databases inner join _user u on u.id = _databases.user_entity_id where email = ? and system_name = ?")
                statement.setString(1, username)
                statement.setString(2, database)
                val resultSet = statement.executeQuery()
                while (resultSet?.next() == true) {
                    for (i in 1..resultSet.metaData.columnCount) {
                        userCredentials.apply {
                            login = resultSet.getString("login")
                            password = resultSet.getString("password_dbms")
                        }
                    }
                }
                resultSet.close()
            }
            userCredentials
        } catch (ex: Exception) {
            logger.error(ex.message)
            null
        }
    }
}