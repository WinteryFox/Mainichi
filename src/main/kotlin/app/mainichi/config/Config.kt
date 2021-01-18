package app.mainichi.config

import app.mainichi.component.AuthenticationSuccessHandler
import app.mainichi.component.LogoutSuccessHandler
import app.mainichi.component.LongSerializer
import app.mainichi.component.OAuth2AuthorizationRequestResolver
import app.mainichi.data.Storage
import app.mainichi.session.AttributeService
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.codec.CodecCustomizer
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
    ): SecurityWebFilterChain {
        if (debug)
            httpSecurity
                .cors()
                .configurationSource {
                    CorsConfiguration()
                        .apply {
                            allowedOrigins = listOf("http://localhost:8080")
                            allowCredentials = true
                            allowedMethods = listOf("*")
                            allowedHeaders = listOf("*")
                        }
                }

        return httpSecurity
            .csrf()
            .disable()
            .authorizeExchange()
            .pathMatchers(
                HttpMethod.GET,
                "/avatars/{hash}.png",
                "/posts",
                "/languages",
                "/users/{id}/posts",
                "/users/{ids}",
                "/users/{id}/languages",
                "/posts/{id}"
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
    }

    @Bean
    fun bucket(): Storage = Storage()

    @Bean
    fun attributeService() = AttributeService()

    @Bean
    fun longSerializerModule(
        serializer: LongSerializer
    ): SimpleModule =
        SimpleModule()
            .addSerializer(serializer)
}