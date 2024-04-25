package ru.dataquire.authorizationservice.controller

import org.springframework.web.bind.annotation.*
import ru.dataquire.authorizationservice.dto.request.AuthorizationRequest
import ru.dataquire.authorizationservice.dto.request.RegistrationRequest
import ru.dataquire.authorizationservice.service.AuthorizationService

@RestController
@RequestMapping("/api/auth")
class AuthorizationController(
    private val authorizationService: AuthorizationService
) {
    @PostMapping("/registration")
    fun registration(@RequestBody request: RegistrationRequest) =
        authorizationService.registration(request)

    @PostMapping("/authorization")
    fun authorization(@RequestBody request: AuthorizationRequest) =
        authorizationService.authorization(request)
}