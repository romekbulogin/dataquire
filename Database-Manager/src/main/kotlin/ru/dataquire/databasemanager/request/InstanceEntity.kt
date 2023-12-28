package ru.dataquire.databasemanager.request


data class InstanceEntity(
    var id: String,
    var url: String,
    var username: String,
    var password: String,
    var dbms: String
)
