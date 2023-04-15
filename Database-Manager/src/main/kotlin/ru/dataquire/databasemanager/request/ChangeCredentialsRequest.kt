package ru.dataquire.databasemanager.request

data class ChangeCredentialsRequest(
    var database: String? = null,
    var systemName: String? = null,
    var dbms: String? = null
)
