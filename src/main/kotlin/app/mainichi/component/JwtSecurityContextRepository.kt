package app.mainichi.component

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.lang.UnsupportedOperationException

@Component
class JwtSecurityContextRepository(
    private val authenticationManager: ReactiveAuthenticationManager
) : ServerSecurityContextRepository {
    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        throw UnsupportedOperationException()
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> = mono {
        val header = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?: return@mono null

        val split = header.split(' ')
        if (split.size != 2)
            return@mono null

        val authentication = authenticationManager.authenticate(
            PreAuthenticatedAuthenticationToken(
                split[0],
                split[1]
            )
        ).awaitSingle()

        return@mono SecurityContextImpl(authentication)
    }
}