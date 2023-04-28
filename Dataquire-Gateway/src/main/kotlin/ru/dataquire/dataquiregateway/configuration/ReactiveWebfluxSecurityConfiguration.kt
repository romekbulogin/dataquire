package ru.dataquire.dataquiregateway.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer


@Configuration
@EnableWebFlux
class ReactiveWebfluxSecurityConfiguration: WebFluxConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowCredentials(true)
            .allowedOrigins("*", "http://localhost:3000", "http://localhost:3000/")
            .allowedHeaders("*")
            .allowedMethods("*")
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter? {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowCredentials = true
        corsConfiguration.addAllowedHeader("*")
        corsConfiguration.addAllowedMethod("*")
        corsConfiguration.addAllowedOrigin("*")
        corsConfiguration.addAllowedOrigin("http://localhost:3000")
        corsConfiguration.addAllowedOrigin("http://localhost:3000/")
        val corsConfigurationSource = UrlBasedCorsConfigurationSource()
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)
        return CorsWebFilter(corsConfigurationSource)
    }
}