package app.mainichi.component

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationWebFilter(
    authenticationManager: JwtAuthenticationManager,
    authenticationConverter: JwtAuthenticationConverter
) : AuthenticationWebFilter(authenticationManager) {
    init {
        super.setServerAuthenticationConverter(authenticationConverter)
        super.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        super.setRequiresAuthenticationMatcher { exchange ->
            AndServerWebExchangeMatcher(
                NegatedServerWebExchangeMatcher(
                    ServerWebExchangeMatchers.pathMatchers(
                        HttpMethod.GET,
                        "/users/@me"
                    )
                ),
                ServerWebExchangeMatchers.matchers(
                    ServerWebExchangeMatchers.pathMatchers(
                        HttpMethod.POST,
                        "/login",
                        "/register"
                    ),
                    ServerWebExchangeMatchers.pathMatchers(
                        HttpMethod.GET,
                        "/events",
                        "/avatars/{hash}.png",
                        "/posts",
                        "/users/{id}",
                        "/posts/{id}/comments"
                    )
                )
            )
                .matches(exchange)
                .flatMap {
                    if (it.isMatch)
                        ServerWebExchangeMatcher.MatchResult.notMatch()
                    else
                        ServerWebExchangeMatcher.MatchResult.match()
                }
        }
        super.setAuthenticationFailureHandler { filterExchange, _ ->
            filterExchange.exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return@setAuthenticationFailureHandler Mono.empty<Void>()
        }
    }
}