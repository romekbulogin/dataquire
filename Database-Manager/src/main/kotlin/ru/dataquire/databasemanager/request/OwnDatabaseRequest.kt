package ru.dataquire.databasemanager.request

data class OwnDatabaseRequest(
    var dbms: String? = null,
    var database: String? = null,
    var url: String? = null,
    var login: String? = null,
    var password: String? = null
)
