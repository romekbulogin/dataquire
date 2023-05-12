package ru.dataquire.tablemanager.service

import mu.KotlinLogging
import org.jooq.Field
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.constraint
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.tablemanager.dto.Column
import ru.dataquire.tablemanager.enums.SQLDataTypeEnum
import ru.dataquire.tablemanager.enums.SQLDefaultDateType
import ru.dataquire.tablemanager.feign.InstanceKeeperClient
import ru.dataquire.tablemanager.feign.request.FindInstance
import ru.dataquire.tablemanager.feign.request.InstanceEntity
import ru.dataquire.tablemanager.repository.DatabaseRepository
import ru.dataquire.tablemanager.repository.UserRepository
import ru.dataquire.tablemanager.request.CreateTableRequest
import ru.dataquire.tablemanager.request.ViewTableRequest
import java.sql.DriverManager
import java.sql.ResultSet
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

    fun viewTable(token: String, request: ViewTableRequest): Any {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(currentUser, request.systemName!!)
            val resultQuery = mutableListOf<MutableMap<String, Any?>>()
            var map = mutableMapOf<String, Any?>()


            val connection =
                DriverManager.getConnection(
                    currentDatabase.url,
                    currentDatabase.login,
                    String(
                        decryptCipher.doFinal(
                            Base64.getDecoder()
                                .decode(currentDatabase.passwordDbms)
                        )
                    )
                )
            val resultSet = connection.createStatement().executeQuery("select * from ${request.table}")

            val columns = mutableListOf<Column>()

            val resultSetColumns = connection.metaData.getColumns(null, null, request.table, null)

            while (resultSetColumns.next()) {
                columns.add(Column().apply {
                    field = resultSetColumns.getString("COLUMN_NAME")
                    type = resultSetColumns.getString("TYPE_NAME")
                })
            }

            while (resultSet.next()) {
                for (i in 1..resultSet.metaData.columnCount) {
                    map[resultSet.metaData.getColumnName(i)] = resultSet.getObject(i)
                }
                resultQuery.add(map)
                map = mutableMapOf()
            }
            resultSet.close()
            connection.close()
            ResponseEntity(mapOf("columns" to columns, "rows" to resultQuery), HttpStatus.OK)
        } catch (ex: SQLException) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    //TODO: реализовать метод для обновления данных в какой-либо таблице
    fun updateRawInTable() {

    }

    fun getColumnForForeignKey(token: String, request: ViewTableRequest): ResponseEntity<Any> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(
                    currentUser,
                    request.systemName!!
                )
            val connection = DriverManager.getConnection(
                currentDatabase.url,
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            )
            val columns = mutableListOf<String>()

            val resultSetColumns = connection.metaData.getColumns(null, null, request.table, null)

            while (resultSetColumns.next()) {
                columns.add(resultSetColumns.getString("COLUMN_NAME"))
            }

            resultSetColumns.close()
            connection.close()
            ResponseEntity(columns, HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun dropTable(token: String, systemName: String, tableName: String): ResponseEntity<Map<String, String?>> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(
                    currentUser,
                    systemName
                )
            val connection = DriverManager.getConnection(
                currentDatabase.url,
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            )
            DSL.using(connection).dropTable(tableName).execute()
            connection.close()
            ResponseEntity(mapOf("status" to "success"), HttpStatus.OK)
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
            val connection = DriverManager.getConnection(
                currentDatabase.url,
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

            val fields = mutableListOf<Field<out Any>>()

            val table = dslContext.createTable(request.tableName)

            request.columns.forEach { column ->
                fields.add(
                    DSL.field(
                        column.name,
                        SQLDataTypeEnum.getSqlDataType(column.dataType!!)?.nullable(column.isNull)
                            ?.length(column.length)?.identity(column.isIdentity)
                    )
                )
            }

            for (i in fields.indices) {
                if (request.columns[i].isPrimaryKey)
                    table.primaryKey(fields[i])
                if (request.columns[i].isUnique)
                    table.unique(fields[i])
                if (request.columns[i].isForeignKey) {
                    table.constraints(
                        constraint().foreignKey(fields[i])
                            .references(request.columns[i].targetTable, request.columns[i].targetColumn)
                    )
                }
            }
            table.tableElements(fields)
            table.execute()

            for (i in fields.indices) {
                if (request.columns[i].defaultValue != null) {
                    dslContext.alterTable(request.tableName).alterColumn(fields[i].name)
                        .defaultValue(request.columns[i].defaultValue)
                }
                if (request.columns[i].defaultValue != null || SQLDefaultDateType.getSqlDefaultDateTypes()
                        .contains(request.columns[i].dataType)
                ) {
                    dslContext.alterTable(request.tableName).alterColumn(fields[i].name)
                        .defaultValue(SQLDefaultDateType.getSqlDefaultDateType(request.columns[i].dataType!!))
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
            ResponseEntity(
                mapOf(
                    "status" to ex.message
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    fun getSqlDataTypes() = ResponseEntity(SQLDataTypeEnum.sqlDataTypes(), HttpStatus.OK)

    fun getDefaultDateTypes() = ResponseEntity(SQLDefaultDateType.getSqlDefaultDateTypes(), HttpStatus.OK)
}