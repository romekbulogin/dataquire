package ru.dataquire.databasemanager.request

data class DeleteDatabaseRequest(
    var database: String,
    var dbms: String,
    var systemName: String
)