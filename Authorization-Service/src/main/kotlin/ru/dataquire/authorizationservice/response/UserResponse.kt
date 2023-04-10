package ru.dataquire.authorizationservice.response

import ru.dataquire.authorizationservice.entity.Role


data class UserResponse(
    var username: String = "",
    var email: String = "",
    var isActivated: Boolean? = null,
    var role: Role? = null
)
