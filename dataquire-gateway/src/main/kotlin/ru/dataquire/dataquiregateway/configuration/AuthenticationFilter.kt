package ru.dataquire.dataquiregateway.configuration

import io.jsonwebtoken.Claims
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.dataquire.dataquiregateway.jwt.JwtUtil


@RefreshScope
@Component
class AuthenticationFilter(
    private val routerValidator: RouterValidator,
    private val jwtUtil: JwtUtil
) : GatewayFilter {
    override fun filter(exchange: ServerWebExchange?, chain: GatewayFilterChain?): Mono<Void?>? {
        val request = exchange?.request
        if (routerValidator.isSecured.test(request!!)) {
            if (this.isAuthMissing(request))
                return this.onError(exchange, "Authorization header is missing in request", HttpStatus.UNAUTHORIZED);

            var token = this.getAuthHeader(request)
            token = token?.substring(7, token.length)
            println("TOKEN: $token")

            if (jwtUtil.isInvalid(token!!))
                return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);

            this.populateRequestWithHeaders(exchange, token);
        }
        return chain!!.filter(exchange)
    }

    private fun onError(exchange: ServerWebExchange, err: String, httpStatus: HttpStatus): Mono<Void?> {
        val response: ServerHttpResponse = exchange.response
        response.statusCode = httpStatus
        return response.setComplete()
    }

    private fun getAuthHeader(request: ServerHttpRequest): String? {
        return request.headers.getOrEmpty("Authorization")[0]
    }

    private fun isAuthMissing(request: ServerHttpRequest): Boolean {
        return !request.headers.containsKey("Authorization")
    }

    private fun populateRequestWithHeaders(exchange: ServerWebExchange, token: String) {
        val claims: Claims = jwtUtil.extractAllClaims(token)
        exchange.request.mutate()
            .header("id", claims["id"].toString())
            .header("role", claims["role"].toString())
            .build()
    }

}