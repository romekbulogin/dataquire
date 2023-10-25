package ru.dataquire.databasemanager.request

data class ChangeCredentialsRequest(
    var database: String,
    var systemName: String,
    var dbms: String
)
