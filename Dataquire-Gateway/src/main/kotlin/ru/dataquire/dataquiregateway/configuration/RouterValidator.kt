package ru.dataquire.dataquiregateway.configuration

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
class RouterValidator {
    val openApiEndpoints = listOf(
        "/api/auth/authentication",
        "/api/auth/registration"
    )

    var isSecured: Predicate<ServerHttpRequest> = Predicate<ServerHttpRequest> { request ->
        openApiEndpoints
            .stream()
            .noneMatch { uri: String ->
                println(uri)
                request.uri.path.contains(uri)
            }
    }
}