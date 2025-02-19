package ru.dataquire.authorizationservice.configuration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.dataquire.authorizationservice.service.JwtService

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService, private val userDetailsService: UserDetailsService) :
    OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authenticationHeader: String? = request.getHeader("Authorization")

        require(authenticationHeader != null) {
            filterChain.doFilter(request, response);
            return
        }
        require(authenticationHeader.isNotEmpty() || authenticationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return
        }

        val jwt = authenticationHeader.substring(7)
        val userEmail = jwtService.extractUsername(jwt)

        if (SecurityContextHolder.getContext().authentication == null) {
            val userDetails = this.userDetailsService.loadUserByUsername(userEmail)
            if (jwtService.isTokenValid(jwt, userDetails)) {
                val authToken = UsernamePasswordAuthenticationToken(userEmail, null, userDetails.authorities)
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }
        filterChain.doFilter(request, response)
    }
}