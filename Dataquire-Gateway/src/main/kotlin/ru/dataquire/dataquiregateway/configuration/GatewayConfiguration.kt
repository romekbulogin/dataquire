package ru.dataquire.dataquiregateway.configuration

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfiguration(private val authenticationFilter: AuthenticationFilter) {
    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator? {
        return builder.routes()
            .route("instance-keeper") { r ->
                r.path("/api/instances/**")
                    .filters { f -> f.filter(authenticationFilter) }
                    .uri("lb://instance-keeper")
            }
            .route("authorization-service") { r ->
                r.path("/api/auth/**")
                    .filters { f -> f.filter(authenticationFilter) }
                    .uri("lb://authorization-service")
            }
            .build()
    }
}