package app.mainichi.component

import app.mainichi.service.JwtService
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val jwtService: JwtService
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> = mono {
        try {
            val jws = jwtService.validateToken(authentication.credentials as String)

            return@mono UsernamePasswordAuthenticationToken(
                jws.body.subject,
                authentication.credentials as String,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )
        } catch (_: Exception) {
            throw BadCredentialsException("Token is invalid.")
        }
    }
}