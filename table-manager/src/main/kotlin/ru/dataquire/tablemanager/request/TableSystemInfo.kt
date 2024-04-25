package ru.dataquire.tablemanager.request

data class TableSystemInfo(
    var database: String? = null,
    var dbms: String? = null,
    var systemName: String? = null
)
