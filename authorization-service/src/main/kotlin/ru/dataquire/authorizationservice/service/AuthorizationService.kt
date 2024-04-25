package ru.dataquire.authorizationservice.service

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.dataquire.authorizationservice.dto.Owner
import ru.dataquire.authorizationservice.entity.OwnerEntity
import ru.dataquire.authorizationservice.entity.Role
import ru.dataquire.authorizationservice.repository.OwnerRepository
import ru.dataquire.authorizationservice.dto.request.AuthorizationRequest
import ru.dataquire.authorizationservice.dto.request.RegistrationRequest
import ru.dataquire.authorizationservice.dto.response.RegistrationResponse

@Service
class AuthorizationService(
    private val ownerRepository: OwnerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
) {
    private val logger = LoggerFactory.getLogger(AuthorizationService::class.java)

    fun registration(request: RegistrationRequest): ResponseEntity<*> = try {
        logger.info("[REGISTRATION] username={${request.username}}")

        val owner = ownerRepository.save(
            OwnerEntity().apply {
                username = request.username
                password = passwordEncoder.encode(
                    request.password
                )
                role = Role.USER
            }
        )

        ResponseEntity.ok().body(
            RegistrationResponse(
                jwtService.generateToken(owner),
                Owner(owner.username, owner.role.name)
            )
        )
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity.badRequest().body(
            mapOf("error" to ex.message)
        )
    }

    fun authorization(request: AuthorizationRequest): ResponseEntity<*> = try {
        logger.info("[AUTHORIZATION] username={${request.username}}")
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.username, request.password
            )
        )
        val owner = ownerRepository.findByUsername(request.username)
        ResponseEntity.ok().body(
            RegistrationResponse(
                jwtService.generateToken(owner),
                Owner(owner.username, owner.role.name)
            )
        )
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity.badRequest().body(
            mapOf("error" to ex.message)
        )
    }
}