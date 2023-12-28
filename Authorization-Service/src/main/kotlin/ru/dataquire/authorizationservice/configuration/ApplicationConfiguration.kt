package ru.dataquire.authorizationservice.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import ru.dataquire.authorizationservice.repository.OwnerRepository


@Configuration
class ApplicationConfiguration(
    private val ownerRepository: OwnerRepository
) {

    @Bean
    fun userDetailsService(): UserDetailsService? {
        return UserDetailsService { username: String ->
            ownerRepository.findByEmail(username)
                .orElseThrow { UsernameNotFoundException("User not found") }
        }
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): AuthenticationProvider = DaoAuthenticationProvider().apply {
        setUserDetailsService(userDetailsService())
        setPasswordEncoder(passwordEncoder())
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager
}
