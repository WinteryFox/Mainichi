package app.mainichi.config

import app.mainichi.component.AuthenticationSuccessHandler
import app.mainichi.data.Storage
import app.mainichi.session.AttributeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import reactor.core.publisher.Mono

/**
 * Configures Spring Security, so that certain pages and actions
 * can only be performed by authorized (logged in) users (e.g. requesting
 * user data or requesting database updates).
 */
@Configuration
@EnableWebFluxSecurity
class Config(
    private val authenticationSuccessHandler: AuthenticationSuccessHandler
) {
    /**
     * Configures REST endpoints security
     */
    @Bean
    fun securityFilterChain(
        httpSecurity: ServerHttpSecurity
    ): SecurityWebFilterChain = httpSecurity
        .csrf()
        .disable() // TODO: Enable when testing finishes
        .cors()
        .configurationSource {
            val cors = CorsConfiguration()
            cors.allowedOrigins = listOf("*")
            return@configurationSource cors
        }
        .and()
        .authorizeExchange()
        .pathMatchers(
            HttpMethod.GET,
            "/login",
            "/register",
            "/avatars/*.png",
            "/posts",
            "/users/*/posts",
            "/users/{snowflakes}"
        ).permitAll()
        .anyExchange().authenticated() // Any other requests must be authenticated
        .and()
        .oauth2Login(::withConfiguration) // Sets up OAuth2 login (with Google and eventual other providers)
        .build()

    /**
     * Sets up a handler for successful and unsuccessful login attempts.
     * A successful login attempt will execute [AuthenticationSuccessHandler]
     *
     * @see AuthenticationSuccessHandler
     */
    fun withConfiguration(spec: ServerHttpSecurity.OAuth2LoginSpec): Unit =
        spec
            .authenticationSuccessHandler(authenticationSuccessHandler)
            .authenticationFailureHandler { _, error ->
                error.printStackTrace()
                return@authenticationFailureHandler Mono.empty<Void>()
            }
            .run {}

    @Bean
    fun bucket(): Storage = Storage()

    @Bean
    fun attributeService() = AttributeService()
}