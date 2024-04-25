package ru.dataquire.tablemanager

import org.jooq.Field
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ru.dataquire.tablemanager.enums.SQLDataTypeEnum
import ru.dataquire.tablemanager.enums.SQLDefaultDateType
import ru.dataquire.tablemanager.request.Column
import ru.dataquire.tablemanager.request.CreateTableRequest
import ru.dataquire.tablemanager.request.TableSystemInfo
import java.sql.DriverManager
import java.util.*

@SpringBootTest
class TableManagerApplicationTests {

//    @Test
//    fun contextLoads() {
//        val connection = DriverManager.getConnection(
//            "jdbc:postgresql://185.124.64.2:5432/ztfbzhdpruymmrc2ve05uxvkbnrb0ox00r64bq7m",
//            "postgres",
//            "1337"
//        )
////        val catalogs = connection.metaData.getColumns(connection.catalog, null, "cooltb", null)
////        val list = mutableListOf<String>()
////        while (catalogs.next()) {
////            list.add(catalogs.getString("COLUMN_NAME"))
////
////        }
//        val dslContext = DSL.using(connection, SQLDialect.POSTGRES)
//
//        val fields = mutableListOf<Field<out Any>>()
//
//        val request = CreateTableRequest().apply {
//            this.tableSystemInfo = TableSystemInfo("testdb2", "PostgreSQL", "ztfbzhdpruymmrc2ve05uxvkbnrb0ox00r64bq7m")
//            this.tableName = "cooltb3"
//            this.columns = mutableListOf<Column>().apply {
//                add(Column().apply {
//                    this.name = "id"
//                    this.dataType = "INTEGER"
//                    this.isPrimaryKey = true
//                    this.isIdentity = true
//                })
//                add(Column().apply {
//                    this.name = "firstname_id"
//                    this.dataType = "INTEGER"
//                    this.isForeignKey = true
//                    this.targetTable = "cooltb"
//                    this.targetColumn = "id"
//                })
//            }
//        }
//
//        val table = dslContext.createTable(request.tableName)
//
//        request.columns.forEach { column ->
//            fields.add(
//                field(
//                    column.name,
//                    SQLDataTypeEnum.getSqlDataType(column.dataType!!)?.nullable(column.isNull)
//                        ?.length(column.length)?.identity(column.isIdentity)
//                )
//            )
//        }
//
//        for (i in fields.indices) {
//            if (request.columns[i].isPrimaryKey)
//                table.primaryKey(fields[i])
//            if (request.columns[i].isUnique)
//                table.unique(fields[i])
//            if (request.columns[i].isForeignKey) {
//                table.constraints(
//                    constraint().foreignKey(fields[i])
//                        .references(request.columns[i].targetTable, request.columns[i].targetColumn)
//                )
//            }
//        }
//        table.tableElements(fields)
//        table.execute()
//
//        for (i in fields.indices) {
//            if (request.columns[i].defaultValue != null) {
//                dslContext.alterTable(request.tableName).alterColumn(fields[i].name)
//                    .defaultValue(request.columns[i].defaultValue)
//            }
//            if (request.columns[i].defaultValue != null || SQLDefaultDateType.getSqlDefaultDateTypes()
//                    .contains(request.columns[i].dataType)
//            ) {
//                dslContext.alterTable(request.tableName).alterColumn(fields[i].name)
//                    .defaultValue(SQLDefaultDateType.getSqlDefaultDateType(request.columns[i].dataType!!))
//            }
//        }
//    }
}
