package app.mainichi.component

import kotlinx.coroutines.reactive.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class OAuth2AuthorizationRequestResolver(
    registration: ReactiveClientRegistrationRepository
) : ServerOAuth2AuthorizationRequestResolver {
    private val resolver = DefaultServerOAuth2AuthorizationRequestResolver(registration)

    override fun resolve(
        exchange: ServerWebExchange
    ): Mono<OAuth2AuthorizationRequest> = mono {
        val request = resolver.resolve(exchange).awaitSingleOrNull()
        if (request != null)
            return@mono customizeRequest(exchange, request)

        return@mono null
    }

    override fun resolve(
        exchange: ServerWebExchange,
        clientRegistrationId: String
    ): Mono<OAuth2AuthorizationRequest> = mono {
        val request = resolver.resolve(exchange, clientRegistrationId).awaitSingleOrNull()
        if (request != null)
            return@mono customizeRequest(exchange, request)

        return@mono null
    }

    suspend fun customizeRequest(
        exchange: ServerWebExchange,
        request: OAuth2AuthorizationRequest
    ): OAuth2AuthorizationRequest {
        if (exchange.request.queryParams.containsKey("redirect_uri"))
            exchange.awaitSession().attributes["redirect_uri"] = exchange.request.queryParams["redirect_uri"]!![0]

        return request
    }
}