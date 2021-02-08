package app.mainichi.controller

import app.mainichi.ErrorCode
import app.mainichi.request.AccountCreateRequest
import app.mainichi.request.LoginRequest
import app.mainichi.response.ReCaptchaTokenResponse
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.repository.UserRepository
import app.mainichi.response.LoginSuccessResponse
import app.mainichi.service.JwtService
import app.mainichi.service.SnowflakeService
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.r2dbc.core.bind
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant

@RestController
class AccountController(
    private val jwtService: JwtService,
    private val snowflakeService: SnowflakeService,
    private val userRepository: UserRepository,
    private val client: DatabaseClient
) {
    @PostMapping("/register")
    suspend fun createAccount(
        @RequestBody
        request: AccountCreateRequest
    ): LoginSuccessResponse {
        if (userRepository.findByEmail(request.email) != null)
            throw ResponseStatusCodeException(ErrorCode.USER_EXISTS)

        val id = snowflakeService.next()

        client.sql(
            """
INSERT INTO users (id, email, username, gender, birthday, summary, avatar, password, version)
VALUES ($1, $2, $3, $4, $5, $6, $7, crypt($8, gen_salt('md5')), $9)
RETURNING *
        """
        )
            .bind(0, id)
            .bind(1, request.email)
            .bind(2, request.username)
            .bind(3, request.gender)
            .bind(4, request.birthday)
            .bind(5, request.summary)
            .bindNull(6, String::class.java)
            .bind(7, request.password)
            .bind(8, 1)
            .then()
            .awaitSingleOrNull()

        return LoginSuccessResponse(
            jwtService.createToken(id, Instant.now().plus(Duration.ofDays(30))) // TODO: Maybe use a refresh token?
        )
    }

    @PostMapping("/login")
    suspend fun login(
        @RequestBody
        request: LoginRequest
    ): LoginSuccessResponse {
        val id = client.sql(
            """
SELECT (password = crypt($2, password)) AS matches, id AS id
FROM users
WHERE email = $1 
            """
        )
            .bind(0, request.email)
            .bind(1, request.password)
            .map { row, _ ->
                if (!(row["matches"] as Boolean))
                    throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

                row["id"] as Long
            }
            .awaitSingle()

        return LoginSuccessResponse(
            jwtService.createToken(id, Instant.now().plus(Duration.ofDays(30))) // TODO: Maybe use a refresh token?
        )
    }
}
