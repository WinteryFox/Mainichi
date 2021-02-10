package app.mainichi.config

import app.mainichi.component.JwtAuthenticationWebFilter
import app.mainichi.component.LongSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebFluxSecurity
class Config {
    @Bean
    fun securityFilterChain(
        httpSecurity: ServerHttpSecurity,
        authenticationWebFilter: JwtAuthenticationWebFilter,
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
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .requestCache().disable()
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