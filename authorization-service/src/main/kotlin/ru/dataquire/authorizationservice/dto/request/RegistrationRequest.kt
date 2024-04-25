package ru.dataquire.authorizationservice.dto.request

data class RegistrationRequest(
    val username: String,
    val password: String,
    val secondPassword: String
)