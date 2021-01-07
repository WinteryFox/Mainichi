package app.mainichi.config

import app.mainichi.component.AuthenticationSuccessHandler
import app.mainichi.component.LogoutSuccessHandler
import app.mainichi.component.OAuth2AuthorizationRequestResolver
import app.mainichi.data.Storage
import app.mainichi.session.AttributeService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration

/**
 * Configures Spring Security, so that certain pages and actions
 * can only be performed by authorized (logged in) users (e.g. requesting
 * user data or requesting database updates).
 */
@Configuration
@EnableWebFluxSecurity
class Config(
    private val authorizationRequestResolver: OAuth2AuthorizationRequestResolver,
    private val authenticationSuccessHandler: AuthenticationSuccessHandler,
    private val logoutSuccessHandler: LogoutSuccessHandler
) {
    /**
     * Configures REST endpoints security
     */
    @Bean
    fun securityFilterChain(
        httpSecurity: ServerHttpSecurity,
        @Value("\${debug}")
        debug: Boolean
    ): SecurityWebFilterChain = httpSecurity
        .csrf()
        .disable() // TODO: Enable when testing finishes
        .cors()
        .configurationSource {
            CorsConfiguration()
                .apply {
                    if (debug) {
                        allowedOrigins = listOf("http://localhost:8080")
                        allowCredentials = true
                        allowedMethods = listOf("*")
                    }
                }
        }
        .and()
        .authorizeExchange()
        .pathMatchers(
            HttpMethod.GET,
            "/avatars/{hash}.png",
            "/posts",
            "/users/{snowflake}/posts",
            "/users/{snowflakes}"
        ).permitAll()
        .anyExchange().authenticated() // Any other requests must be authenticated
        .and()
        .httpBasic().disable()
        .formLogin().disable()
        .oauth2Login()
        .authorizationRequestResolver(authorizationRequestResolver)
        .authenticationSuccessHandler(authenticationSuccessHandler)
        .and()
        .logout()
        .logoutSuccessHandler(logoutSuccessHandler)
        .and()
        .build()

    @Bean
    fun bucket(): Storage = Storage()

    @Bean
    fun attributeService() = AttributeService()
}