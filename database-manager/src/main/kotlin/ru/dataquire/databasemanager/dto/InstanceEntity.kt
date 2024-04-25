package ru.dataquire.databasemanager.dto


data class InstanceEntity(
    var id: String,
    var url: String,
    var username: String,
    var password: String,
    var dbms: String
)
