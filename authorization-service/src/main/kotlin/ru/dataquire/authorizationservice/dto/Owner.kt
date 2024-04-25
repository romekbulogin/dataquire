package ru.dataquire.authorizationservice.dto

import ru.dataquire.authorizationservice.entity.Role

data class Owner(
    var username: String,
    var role: String
)