package ru.dataquire.authorizationservice.dto.response

import ru.dataquire.authorizationservice.dto.Owner

data class AuthorizationResponse (
    var token: String,
    var owner: Owner
)