package ru.dataquire.authorizationservice.request

data class QueryRequestAuth(
    var sql: String = "",
    var database: String = "",
    var dbms: String = "",
)
