package app.mainichi.component

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class LogoutSuccessHandler : ServerLogoutSuccessHandler {
    override fun onLogoutSuccess(exchange: WebFilterExchange, authentication: Authentication): Mono<Void> = mono {
        val session = exchange.exchange.session.awaitSingle()
        session.invalidate().awaitFirstOrNull()
    }
}