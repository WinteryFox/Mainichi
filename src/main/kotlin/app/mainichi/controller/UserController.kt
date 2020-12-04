package app.mainichi.controller

import app.mainichi.objects.User
import app.mainichi.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.server.awaitFormData
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange
import java.sql.Date

@RestController
class UserController(
    val userRepository: UserRepository
) {
    @GetMapping("/users/@me", produces = ["application/json"])
    suspend fun getSelf(
        exchange: ServerWebExchange
    ): User = userRepository.getBySnowflake(exchange.awaitSession().attributes["SNOWFLAKE"] as Long)!!

    @RequestMapping("/users/@me", method = [RequestMethod.POST])
    suspend fun updateSelf(
        exchange: ServerWebExchange
    ) {
        val user = userRepository.getBySnowflake(exchange.attributes["SNOWFLAKE"] as Long)!!
        val form = exchange.awaitFormData().toSingleValueMap()

        userRepository.save(
            User(
                user.snowflake,
                user.email,
                form["username"]!!,
                Date.valueOf(form["birthday"]!!),
                form["gender"]!![0],
                form["summary"]!!
            )
        )

        exchange.response.statusCode = HttpStatus.NO_CONTENT
    }
}