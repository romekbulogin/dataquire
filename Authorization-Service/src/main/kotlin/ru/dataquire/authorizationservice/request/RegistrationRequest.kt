package ru.dataquire.authorizationservice.request

data class RegistrationRequest(
    var username: String,
    var email: String,
    var password: String
)