package ru.dataquire.authorizationservice.request

data class DeleteDatabase(
    var database: String? = null,
    var dbms: String? = null
)
