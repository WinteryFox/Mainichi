package app.mainichi.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.*

@Service
class JwtService {
    private val key: KeyPair

    init {
        val factory = KeyFactory.getInstance("RSA")

        key = KeyPair(
            factory.generatePublic(
                X509EncodedKeySpec(Files.readAllBytes(Paths.get(System.getenv("JWT_PUBLIC_KEY"))))
            ),
            factory.generatePrivate(
                PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get(System.getenv("JWT_PRIVATE_KEY"))))
            )
        )
    }

    fun createToken(id: Long, expiry: Instant): String =
        Jwts.builder()
            .signWith(key.private, SignatureAlgorithm.RS256)
            .setSubject(id.toString())
            .setIssuer("identity")
            .setExpiration(Date.from(expiry))
            .setIssuedAt(Date.from(Instant.now()))
            .compact()

    fun validateToken(token: String): Jws<Claims> =
        Jwts.parserBuilder()
            .setSigningKey(key.public)
            .build()
            .parseClaimsJws(token)
}