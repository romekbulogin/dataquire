package ru.dataquire.databasemanager.request

data class OwnDatabaseRequest(
    var dbms: String,
    var database: String,
    var url: String,
    var login: String,
    var password: String
)
