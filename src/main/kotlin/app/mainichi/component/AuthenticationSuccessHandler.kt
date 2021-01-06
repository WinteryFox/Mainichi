package app.mainichi.component

import app.mainichi.table.User
import app.mainichi.repository.UserRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.server.DefaultServerRedirectStrategy
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.reactive.server.awaitSession
import reactor.core.publisher.Mono
import java.net.URI

/**
 * Handler for successful OAuth2 login attempts
 */
@Component
class AuthenticationSuccessHandler(
    val userRepository: UserRepository
) : ServerAuthenticationSuccessHandler {
    val redirect = DefaultServerRedirectStrategy()

    override fun onAuthenticationSuccess(
        filterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> = mono {
        // Get the user that logged in
        val oAuth2User = authentication.principal as DefaultOAuth2User

        // Check if it contains an email
        if (oAuth2User.attributes.containsKey("email")) {
            // Attempt to retrieve a user by that email
            val user = userRepository.findByEmail(oAuth2User.attributes["email"] as String)
            val session = filterExchange.exchange.awaitSession()

            if (user != null) {
                // If such a user is found, put their ID in the session (so we know who this user is later)
                session.attributes.putIfAbsent("SNOWFLAKE", user.snowflake.toString())
            } else {
                // If such a user does not exist, create an account using that email, and then put
                // the newly created snowflake in the session
                session.attributes.putIfAbsent(
                    "SNOWFLAKE",
                    userRepository.save(
                        User(
                            0,
                            oAuth2User.attributes["email"] as String,
                            oAuth2User.attributes["name"] as String,
                            null,
                            null,
                            null,
                            null
                        )
                    ).snowflake.toString()
                )
            }

            if (session.attributes.containsKey("redirect_uri")) {
                redirect.sendRedirect(
                    filterExchange.exchange,
                    URI(session.attributes["redirect_uri"] as String)
                ).awaitFirstOrNull()
                session.attributes.remove("redirect_uri")
            }
        }

        return@mono null
    }
}