package app.mainichi.controller

import app.mainichi.objects.User
import app.mainichi.repository.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange

@RestController
class UserController(
    val userRepository: UserRepository
) {
    @GetMapping("/users/@me", produces = ["application/json"])
    suspend fun getSelf(
        exchange: ServerWebExchange
    ): User {
        println(exchange.awaitSession().attributes["SNOWFLAKE"])
        return userRepository.getBySnowflake(exchange.awaitSession().attributes["SNOWFLAKE"] as Long)!!
    }
}