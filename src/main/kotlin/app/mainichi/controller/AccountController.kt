package app.mainichi.controller

import app.mainichi.ErrorCode
import app.mainichi.request.AccountCreateRequest
import app.mainichi.request.LoginRequest
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.filter.ReCaptchaService
import app.mainichi.repository.UserRepository
import app.mainichi.response.LoginSuccessResponse
import app.mainichi.service.JwtService
import app.mainichi.service.SnowflakeService
import org.springframework.http.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant

@RestController
class AccountController(
    private val jwtService: JwtService,
    private val snowflakeService: SnowflakeService,
    private val reCaptchaService: ReCaptchaService,
    private val userRepository: UserRepository
) {
    @PostMapping("/register")
    suspend fun createAccount(
        @RequestBody
        request: AccountCreateRequest
    ): LoginSuccessResponse {
        if (!reCaptchaService.validate(request.captcha))
            throw ResponseStatusCodeException(ErrorCode.FAILED_CAPTCHA)
        if (userRepository.findByEmail(request.email) != null)
            throw ResponseStatusCodeException(ErrorCode.USER_EXISTS)

        val user = userRepository.register(
            snowflakeService.next(),
            request.email,
            request.username,
            request.gender,
            request.birthday,
            request.summary,
            request.password,
        )

        return LoginSuccessResponse(
            jwtService.createToken(user.id, Instant.now().plus(Duration.ofDays(30))) // TODO: Maybe use a refresh token?
        )
    }

    @PostMapping("/login")
    suspend fun login(
        @RequestBody
        request: LoginRequest
    ): LoginSuccessResponse {
        val user = userRepository.login(request.email, request.password)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        return LoginSuccessResponse(
            jwtService.createToken(user.id, Instant.now().plus(Duration.ofDays(30))) // TODO: Maybe use a refresh token?
        )
    }
}
