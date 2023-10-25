package ru.dataquire.databasemanager.request


data class InstanceEntity(
    var id: Int,
    var url: String,
    var username: String,
    var password: String,
    var dbms: String
)
