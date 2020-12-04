package app.mainichi.component

import app.mainichi.objects.User
import app.mainichi.repository.UserRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.server.DefaultServerRedirectStrategy
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.reactive.server.awaitSession
import reactor.core.publisher.Mono
import java.net.URI

@Component
class AuthenticationSuccessHandler(
    val userRepository: UserRepository
) : ServerAuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> = mono {
        val oAuth2User = authentication.principal as DefaultOAuth2User

        if (oAuth2User.attributes.containsKey("email")) {
            val user = userRepository.getByEmail(oAuth2User.attributes["email"] as String)

            if (user != null)
                webFilterExchange.exchange.awaitSession().attributes.putIfAbsent("SNOWFLAKE", user.snowflake)
            else
                webFilterExchange.exchange.awaitSession().attributes.putIfAbsent(
                    "SNOWFLAKE",
                    userRepository.save(
                        User(
                            0,
                            oAuth2User.attributes["email"] as String,
                            oAuth2User.attributes["name"] as String,
                            null,
                            null,
                            null
                        )
                    ).snowflake
                )
        }

        return@mono null
        /*DefaultServerRedirectStrategy()
            .sendRedirect(webFilterExchange.exchange, URI("http://localhost:8080"))
            .awaitFirstOrNull()*/ // TODO
    }
}