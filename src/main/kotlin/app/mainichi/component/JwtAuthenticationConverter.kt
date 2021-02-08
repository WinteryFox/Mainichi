package app.mainichi.component

import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> = mono {
        val header = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?: return@mono PreAuthenticatedAuthenticationToken(null, null)

        val split = header.split(' ')
        if (split.size != 2)
            return@mono PreAuthenticatedAuthenticationToken(null, null)

        return@mono PreAuthenticatedAuthenticationToken(
            split[0],
            split[1]
        )
    }
}