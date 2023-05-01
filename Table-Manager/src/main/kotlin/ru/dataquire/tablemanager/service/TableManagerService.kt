package ru.dataquire.tablemanager.service

import jakarta.xml.bind.DatatypeConverter
import mu.KotlinLogging
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.currentDate
import org.jooq.impl.DateAsTimestampBinding
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.tablemanager.enums.SQLDataTypeEnum
import ru.dataquire.tablemanager.enums.SQLDefaultDateType
import ru.dataquire.tablemanager.feign.InstanceKeeperClient
import ru.dataquire.tablemanager.feign.request.FindInstance
import ru.dataquire.tablemanager.feign.request.InstanceEntity
import ru.dataquire.tablemanager.repository.DatabaseRepository
import ru.dataquire.tablemanager.repository.UserRepository
import ru.dataquire.tablemanager.request.CreateTableRequest
import ru.dataquire.tablemanager.request.ViewTableRequest
import java.nio.charset.Charset
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.crypto.Cipher

@Service
class TableManagerService(
    private val instanceKeeperClient: InstanceKeeperClient,
    private val userRepository: UserRepository,
    private val databaseRepository: DatabaseRepository,
    private val jwtService: JwtService,
    private val decryptCipher: Cipher
) {
    private val logger = KotlinLogging.logger { }
    private fun findDriver(driverName: String): InstanceEntity? {
        return try {
            instanceKeeperClient.findInstanceByDbms(FindInstance(driverName))
        } catch (ex: Exception) {
            logger.error(ex.message)
            throw RuntimeException("DBMS url not found")
        }
    }

    fun viewTable(token: String, request: ViewTableRequest): Any {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(currentUser, request.systemName!!)
            val targetDatabase = findDriver(currentDatabase.dbms!!)
            val resultQuery = mutableListOf<MutableMap<String, Any?>>()
            var map = mutableMapOf<String, Any?>()

            logger.info(
                "SIZE OF BYTE ARRAY: " + Base64.getDecoder().decode(
                    currentDatabase.passwordDbms?.toByteArray(
                        Charset.defaultCharset()
                    )
                ).size
            )
            val connection =
                DriverManager.getConnection(
                    "${targetDatabase?.url}${currentDatabase.systemName}",
                    currentDatabase.login,
                    String(
                        decryptCipher.doFinal(
                            Base64.getDecoder()
                                .decode(currentDatabase.passwordDbms?.toByteArray(Charset.defaultCharset()))
                        )
                    )
                )
            val resultSet = connection.createStatement().executeQuery("select * from ${request.table}")


            while (resultSet.next()) {
                for (i in 1..resultSet.metaData.columnCount) {
                    map[resultSet.metaData.getColumnName(i)] = resultSet.getObject(i)
                }
                resultQuery.add(map)
                map = mutableMapOf()
            }
            resultSet.close()
            connection.close()
            ResponseEntity(resultQuery, HttpStatus.OK)
        } catch (ex: SQLException) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    //TODO: реализовать метод для обновления данных в какой-либо таблице
    fun updateRawInTable() {

    }

    fun getColumnsOfTable(token: String, systemName: String, table: String): ResponseEntity<Any> {
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
            val columns = mutableListOf<Map<String, String>>()

            val resultSet = connection.metaData.getColumns(null, null, table, null)

            while (resultSet.next()) {
                columns.add(mapOf("field" to resultSet.getString("COLUMN_NAME")))
            }
            resultSet.close()
            connection.close()
            ResponseEntity(columns, HttpStatus.OK)
        } catch (ex: Exception) {
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun getColumnForForeignKey(token: String, request: ViewTableRequest): ResponseEntity<Map<String, Any?>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(
                    currentUser,
                    request.systemName!!
                )
            val targetDatabase = findDriver(currentDatabase.dbms!!)
            val connection = DriverManager.getConnection(
                "${targetDatabase?.url}${request.systemName}",
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            )
            logger.info { connection.schema }
            val rs = connection.metaData.getCrossReference(
                connection.catalog,
                connection.schema,
                "cooltb",
                null,
                connection.schema,
                "cooltb2"
            )
            val metaDataRs = rs.metaData
            val tables = mutableListOf<String>()

            while (rs.next()) {
                for (i in 1 until metaDataRs.columnCount) {
                    tables.add(rs.getString(i))
                }
            }
            rs.close()
            connection.close()
            ResponseEntity(mapOf("response" to tables), HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun createTable(token: String, request: CreateTableRequest): ResponseEntity<Map<String, String?>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(
                    currentUser,
                    request.tableSystemInfo?.systemName!!
                )
            val targetDatabase = findDriver(currentDatabase.dbms!!)
            val connection = DriverManager.getConnection(
                "${targetDatabase?.url}${request.tableSystemInfo?.systemName}",
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            )

            val dslContext = if (currentDatabase.dbms!! == "PostgreSQL")
                DSL.using(connection, SQLDialect.POSTGRES)
            else
                DSL.using(connection, SQLDialect.valueOf(currentDatabase.dbms!!))


            logger.debug { request }
            val createTable = dslContext.createTable(request.tableName)
            request.primaryKey?.name = "${request.tableName}_constraint_pk"

            //create primary key
            if (request.primaryKey != null) {
                createTable.column(
                    request.primaryKey?.columnName,
                    SQLDataTypeEnum.getSqlDataType(request.primaryKey?.dataType!!)
                        ?.identity(request.primaryKey?.isIdentity!!)
                        ?.length(request.primaryKey?.length!!)
                ).constraints(
                    constraint(request.primaryKey?.name).primaryKey(request.primaryKey?.columnName)
                )
            }


            //create simple columns
            request.columns.forEach {
                createTable.column(
                    it.name,
                    SQLDataTypeEnum.getSqlDataType(it.dataType!!)?.sqlDataType?.nullable(it.isNull!!)?.length(it.length)
                        ?.identity(it.isIdentity!!)
                )
            }

            //create unique value
            if (!request.uniqueAttributes.isNullOrEmpty()) {
                request.uniqueAttributes.forEach {
                    createTable.unique(it)
                }
            }

            //TODO: реализовать поиск подходящих атрибутов для ссылки на внешний ключ (делается через Metadata в JDBC)
            //create foreign keys
            if (!request.foreignKeys.isNullOrEmpty()) {
                request.foreignKeys?.forEach {
                    createTable.column(
                        it.columnName,
                        SQLDataTypeEnum.getSqlDataType(it.dataType!!)?.identity(it.isIdentity!!)
                            ?.length(it.length)?.identity(it.isIdentity!!)
                    ).constraints(
                        constraint(it.name).foreignKey(it.columnName)
                            .references(it.referenceTableName, it.referenceColumnName)
                    )
                }
            }

            //execute query
            createTable.execute()

            //set default value
            if (!request.defaultValues.isNullOrEmpty()) {
                request.defaultValues.forEach { defaultValue ->
                    val currentColumn = request.columns.filter {
                        it.name == defaultValue.key
                    }[0]
                    if (SQLDataTypeEnum.getSqlDateType().contains(currentColumn.dataType))
                        dslContext.alterTable(request.tableName).alterColumn(defaultValue.key)
                            .defaultValue(SQLDefaultDateType.getSqlDefaultDateType(defaultValue.value)).execute()
                    else
                        dslContext.alterTable(request.tableName).alterColumn(defaultValue.key)
                            .defaultValue(defaultValue.value).execute()

                }
            }
            connection.close()
            ResponseEntity(
                mapOf(
                    "status" to "Таблица '${request.tableName}' успешно создана"
                ), HttpStatus.OK
            )

        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun getSqlDataTypes() = ResponseEntity(SQLDataTypeEnum.sqlDataTypes(), HttpStatus.OK)

    fun getDefaultDateTypes() = ResponseEntity(SQLDefaultDateType.getSqlDefaultDateTypes(), HttpStatus.OK)
}