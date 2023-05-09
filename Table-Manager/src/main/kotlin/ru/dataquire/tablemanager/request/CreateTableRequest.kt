package ru.dataquire.tablemanager.request

import ru.dataquire.tablemanager.request.key.ForeignKey
import ru.dataquire.tablemanager.request.key.PrimaryKey

data class CreateTableRequest(
    var tableSystemInfo: TableSystemInfo? = null,
    var tableName: String? = null,
    var columns: MutableList<Column> = mutableListOf(),
)