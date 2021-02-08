package app.mainichi.config

import app.mainichi.component.JwtAuthenticationConverter
import app.mainichi.component.JwtAuthenticationManager
import app.mainichi.component.LongSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.cors.CorsConfiguration
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class Config {
    @Bean
    fun securityFilterChain(
        httpSecurity: ServerHttpSecurity,
        authenticationManager: JwtAuthenticationManager,
        authenticationConverter: JwtAuthenticationConverter,
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

        val authenticationFilter = AuthenticationWebFilter(authenticationManager)
        authenticationFilter.setServerAuthenticationConverter(authenticationConverter)
        authenticationFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        authenticationFilter.setRequiresAuthenticationMatcher { exchange ->
            ServerWebExchangeMatchers.pathMatchers(
                HttpMethod.POST,
                "/login",
                "/register"
            )
                .matches(exchange)
                .flatMap {
                    if (it.isMatch)
                        ServerWebExchangeMatcher.MatchResult.notMatch()
                    else
                        ServerWebExchangeMatcher.MatchResult.match()
                }
        }
        authenticationFilter.setAuthenticationFailureHandler { filterExchange, _ ->
            filterExchange.exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return@setAuthenticationFailureHandler Mono.empty<Void>()
        }

        return httpSecurity
            .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
            .csrf().disable()
            .build()
    }

    @Bean
    fun longSerializerModule(
        serializer: LongSerializer
    ): SimpleModule =
        SimpleModule()
            .addSerializer(serializer)
}