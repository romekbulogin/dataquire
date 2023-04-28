package ru.dataquire.dataquiregateway.configuration

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity

import org.springframework.security.web.server.SecurityWebFilterChain


@EnableWebFluxSecurity
class ReactiveWebfluxSecurityConfiguration {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http
            .csrf { csrf -> csrf.disable() }
        return http.build()
    }
}