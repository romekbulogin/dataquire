package ru.dataquire.databasemanager.request

data class DeleteDatabaseServerRequest(
    var database: String,
    var title: String,
    var dbms: String,
    var systemName: String
)
