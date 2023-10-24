package ru.dataquire.authorizationservice.service

import io.jsonwebtoken.JwtException
import jakarta.mail.internet.AddressException
import jakarta.mail.internet.InternetAddress
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.dataquire.authorizationservice.entity.Role
import ru.dataquire.authorizationservice.entity.OwnerEntity
import ru.dataquire.authorizationservice.repository.OwnerRepository
import ru.dataquire.authorizationservice.request.AuthenticationRequest
import ru.dataquire.authorizationservice.request.RegistrationRequest
import java.util.*

@Service
class AuthenticationService(
    private val ownerRepository: OwnerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val mailService: MailService
) {
    private val logger = KotlinLogging.logger { }

    @Value("\${dataquire.address}")
    private val address: String? = null

    fun registration(request: RegistrationRequest): Map<String, Any> = try {
        logger.info("Registration: $request")

        with(request) {
            require(
                username.isNotEmpty() &&
                        password.isNotEmpty() &&
                        isValidEmailAddress(email)
            ) {
                AddressException("Email is not valid")
            }
        }

        val user = OwnerEntity().apply {
            setUsername(request.username)
            setEmail(request.email)
            setPassword(passwordEncoder.encode(request.password))
            setIsActivated(false)
            setRole(Role.INACTIVE_USER)
            setActivatedUUID(UUID.randomUUID().toString())
        }
        ownerRepository.save(user)
        mailService.sendMessageVerify(
            request.email,
            "Verify your email",
            "$address/email/verify/${user.getActivatedUUID()}"
        )

        mapOf(
            "token" to jwtService.generateToken(user),
            "user" to mapOf(
                "username" to user.getNickname(),
                "email" to user.getEmail(),
                "isActivated" to user.getIsActivated(),
                "role" to user.getRole()
            )
        )
    } catch (ex: Exception) {
        logger.error(ex.message)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create account")
    }

    fun authentication(request: AuthenticationRequest): Map<String, Any> = try {
        logger.info("Authentication: $request")
        with(request) {
            require(
                email.isNotEmpty() &&
                        password.isNotEmpty() &&
                        isValidEmailAddress(email)
            ) {
                throw AddressException("Email is not valid")
            }
        }
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(request.email, request.password))
        val user = ownerRepository.findByEmail(request.email).orElseThrow()

        mapOf(
            "token" to jwtService.generateToken(user),
            "user" to mapOf(
                "username" to user.getNickname(),
                "email" to user.getEmail(),
                "isActivated" to user.getIsActivated(),
                "role" to user.getRole()
            )
        )
    } catch (ex: Exception) {
        logger.error(ex.message)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not found", ex)
    }

    fun refresh(token: String) = try {
        logger.info("Refresh: $token")

        require(token.isNotEmpty()) {
            JwtException("Token is empty")
        }

        val user = ownerRepository.findByEmail(jwtService.extractUsername(token.substring(7))).orElseThrow()
        mapOf(
            "token" to jwtService.generateToken(user),
            "user" to mapOf(
                "username" to user.getNickname(),
                "email" to user.getEmail(),
                "isActivated" to user.getIsActivated(),
                "role" to user.getRole()
            )
        )
    } catch (ex: Exception) {
        logger.error(ex.message)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update account")
    }

    fun verify(uuid: String) = try {
        logger.debug("Verify by UUID: $uuid")

        val user = ownerRepository.findByActivatedUUID(uuid)

        with(user) {
            setIsActivated(true)
            setRole(Role.USER)
        }
        ownerRepository.save(user)
        mapOf("status" to "Verify success")
    } catch (ex: Exception) {
        logger.error(ex.message)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verify failed")
    }

    fun repeatVerify(token: String) = try {
        val uuid = UUID.randomUUID().toString()

        val currentUser = ownerRepository.findByEmail(
            jwtService.extractUsername(
                token.substring(7)
            )
        ).get()
        currentUser.setActivatedUUID(uuid)

        mailService.sendMessageVerify(
            currentUser.getEmail().toString(),
            "Verify your email",
            "$address/email/verify/${uuid}"
        )
        mapOf("status" to "Message delivered successfully")
    } catch (ex: Exception) {
        logger.error(ex.message)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Error sending message")
    }

    fun isValidEmailAddress(email: String): Boolean {
        val emailAddr = InternetAddress(email)
        emailAddr.validate()
        return true
    }
}