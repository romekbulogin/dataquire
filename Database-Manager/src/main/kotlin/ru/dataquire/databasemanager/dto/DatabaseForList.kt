package ru.dataquire.databasemanager.dto

data class DatabaseForList(
    var dbms: String? = null,
    var databaseName: String? = null,
    var systemName: String? = null
)
