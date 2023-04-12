package ru.dataquire.querycreator.request

data class QueryRequest(
    var sql: String = "",
    var database: String = "",
    var dbms: String = "",
)
