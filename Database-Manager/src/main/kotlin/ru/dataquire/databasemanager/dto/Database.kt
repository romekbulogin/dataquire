package ru.dataquire.databasemanager.dto

data class Database(
    var dbms: String,
    var systemName: String,
    var databaseName: String,
    var login: String,
    var url: String,
    var passwordDbms: String,
)