package ru.dataquire.tablemanager.service

import mu.KotlinLogging
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.tablemanager.dto.Column
import ru.dataquire.tablemanager.enums.SQLDataTypeEnum
import ru.dataquire.tablemanager.enums.SQLDefaultDateType
import ru.dataquire.tablemanager.repository.DatabaseRepository
import ru.dataquire.tablemanager.repository.UserRepository
import ru.dataquire.tablemanager.request.CreateTableRequest
import ru.dataquire.tablemanager.request.ViewTableRequest
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.crypto.Cipher


@Service
class TableManagerService(
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
            val columns = mutableListOf<Column>()

            DriverManager.getConnection(
                currentDatabase.url,
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder()
                            .decode(currentDatabase.passwordDbms)
                    )
                )
            ).use { connection ->
                val resultSet = connection.createStatement().executeQuery("select * from ${request.table}")


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
            }
            ResponseEntity(mapOf("columns" to columns, "rows" to resultQuery), HttpStatus.OK)
        } catch (ex: SQLException) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun updateRowInTable(
        token: String,
        tableName: String,
        systemName: String,
        rows: List<Map<String, Any?>>
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            var countForDelete = 0
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(currentUser, systemName)
            DriverManager.getConnection(
                currentDatabase.url,
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder()
                            .decode(currentDatabase.passwordDbms)
                    )
                )
            ).use { connection ->
                val dslContext = using(connection)
                val conditions = mutableListOf<Condition>()
                if (rows.size == 1) {
                    dslContext.insertInto(table(tableName)).set(rows[0]).execute()
                }
                rows[0].forEach { (key, value) ->
                    conditions.add(field(key).eq(value))
                }
                rows[1].forEach { (key, value) ->
                    if (value == null) {
                        countForDelete += 1
                        if (countForDelete == rows[1].size) {
                            dslContext.deleteFrom(table(tableName)).where(conditions).execute()
                            countForDelete = 0
                        }
                    }
                }
                dslContext.update(table(tableName)).set(rows[1]).where(conditions).execute()
            }
            ResponseEntity(mapOf("status" to true), HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun getColumnForForeignKey(token: String, request: ViewTableRequest): ResponseEntity<Any> {
        return try {
            val currentUser = userRepository.findByEmail(jwtService.extractUsername(token.substring(7)))
            val currentDatabase =
                databaseRepository.findDatabaseEntityByUserEntityAndSystemName(
                    currentUser,
                    request.systemName!!
                )
            val columns = mutableListOf<String>()
            DriverManager.getConnection(
                currentDatabase.url,
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            ).use { connection ->
                val resultSetColumns = connection.metaData.getColumns(null, null, request.table, null)

                while (resultSetColumns.next()) {
                    columns.add(resultSetColumns.getString("COLUMN_NAME"))
                }
            }

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
            DriverManager.getConnection(
                currentDatabase.url,
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            ).use { connection ->
                using(connection).dropTable(tableName).execute()
            }
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
            DriverManager.getConnection(
                currentDatabase.url,
                currentDatabase.login,
                String(
                    decryptCipher.doFinal(
                        Base64.getDecoder().decode(currentDatabase.passwordDbms)
                    )
                )
            ).use { connection ->
                val dslContext = using(connection)
                val table = dslContext.createTable(request.tableName)
                val fields = mutableListOf<Field<out Any>>()

                request.columns.forEach { column ->
                    fields.add(
                        field(
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
            }
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