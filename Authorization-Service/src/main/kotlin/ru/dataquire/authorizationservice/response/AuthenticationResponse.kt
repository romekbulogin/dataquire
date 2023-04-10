package ru.dataquire.authorizationservice.response

data class AuthenticationResponse(
    var token: String? = null,
    var user: UserResponse? = null
)