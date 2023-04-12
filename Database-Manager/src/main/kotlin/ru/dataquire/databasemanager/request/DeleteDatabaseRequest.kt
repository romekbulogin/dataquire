package ru.dataquire.databasemanager.request

data class DeleteDatabaseRequest(
    var database: String? = null,
    var dbms: String? = null,
    var systemName: String? = null
)