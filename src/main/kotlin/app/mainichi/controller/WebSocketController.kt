package app.mainichi.controller

import app.mainichi.objects.Post
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Configuration
@EnableRSocketSecurity
class WebSocketConfiguration {
    @Bean
    fun acceptorInterceptor(security: RSocketSecurity): PayloadSocketAcceptorInterceptor =
            security.authorizePayload {
                it
                        .setup().permitAll()
                        .route("feed").authenticated()
                        .anyRequest().permitAll()
            }.simpleAuthentication(Customizer.withDefaults()).build()

    @Bean
    fun userDetails(): MapReactiveUserDetailsService =
            MapReactiveUserDetailsService(
                    User.withDefaultPasswordEncoder()
                            .username("test")
                            .password("12345")
                            .roles("USER")
                            .build()
            )
}

@Controller
class WebSocketController {
    @Autowired
    private lateinit var client: DatabaseClient

    @MessageMapping("feed")
    fun getFeed(): Flux<Post> =
            client
                    .sql("SELECT * FROM posts")
                    .map { row, _ ->
                        Post(
                                row["snowflake"] as Long,
                                row["author"] as Long,
                                row["content"] as String
                        )
                    }
                    .all()
}