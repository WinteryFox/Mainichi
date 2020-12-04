package app.mainichi.config

import app.mainichi.component.AuthenticationSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationSuccessHandler: AuthenticationSuccessHandler
) {
    @Bean
    fun securityFilterChain(
        httpSecurity: ServerHttpSecurity
    ): SecurityWebFilterChain = httpSecurity
        .csrf() // TODO: Enable when testing finishes
        .disable()
        .authorizeExchange()
        .pathMatchers("/login", "/register").permitAll()
        .pathMatchers("/logout").authenticated()
        .anyExchange().authenticated()
        .and()
        .oauth2Login(::withConfiguration)
        .build()

    fun withConfiguration(spec: ServerHttpSecurity.OAuth2LoginSpec): Unit =
        spec
            .authenticationSuccessHandler(authenticationSuccessHandler)
            .authenticationFailureHandler { webFilterExchange, error ->
                error.printStackTrace()
                return@authenticationFailureHandler Mono.empty<Void>()
            }
            .run {}
}