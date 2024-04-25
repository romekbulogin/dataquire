package ru.dataquire.dataquiregateway.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import java.util.function.Function

@Component
class JwtUtil {
    @Value("\${jwt.secret}")
    private val secret: String? = null

    private var key: Key? = null

    @PostConstruct
    fun init() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }

    fun extractAllClaims(token: String): Claims =
        Jwts
            .parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)!!.body

    fun isTokenExpired(token: String): Boolean = extractExpiration(token).before(Date())

    fun extractExpiration(token: String): Date = extractClaim(token, Claims::getExpiration)

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T =
        claimsResolver.apply(extractAllClaims(token))

    fun isInvalid(token: String): Boolean {
        return isTokenExpired(token)
    }

}