package ru.dataquire.databasemanager.response

data class FailedCreatedDatabase(
    var error: String? = null,
    var exception: String? = null
)