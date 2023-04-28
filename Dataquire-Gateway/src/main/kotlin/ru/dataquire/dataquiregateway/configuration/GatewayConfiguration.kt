package ru.dataquire.dataquiregateway.configuration

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
class GatewayConfiguration(private val authenticationFilter: AuthenticationFilter) {
    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator? {
        return builder.routes()
            .route("database-manager") { r ->
                r.path("/api/database/**")
                    .filters { f -> f.filter(authenticationFilter) }
                    .uri("lb://database-manager")
            }
            .route("authorization-service") { r ->
                r.path("/api/auth/**")
                    .filters { f -> f.filter(authenticationFilter) }
                    .uri("lb://authorization-service")
            }
            .build()
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http
            .csrf { csrf -> csrf.disable() }
        return http.build()
    }
}