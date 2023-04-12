package ru.dataquire.databasemanager.dto

data class Database(
    var dbms: String? = null,
    var systemName: String? = null,
    var databaseName: String? = null,
    var login: String? = null,
    var passwordDbms: String? = null,
)