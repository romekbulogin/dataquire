package ru.dataquire.authorizationservice.entity

import jakarta.persistence.*
import java.util.UUID

data class DatabaseEntityWithoutUser(
    var dbms: String? = null,
    var systemName: UUID? = null,
    var databaseName: String? = null,
    var login: String? = null,
    var passwordDbms: String? = null,
)