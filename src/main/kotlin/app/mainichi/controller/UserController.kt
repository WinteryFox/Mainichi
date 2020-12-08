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
import java.time.LocalDate

/**
 * REST controller for user data
 */
@RestController
class UserController(
    val userRepository: UserRepository
) {
    /**
     * Request own user data
     */
    @GetMapping("/users/@me", produces = ["application/json"])
    suspend fun getSelf(
        exchange: ServerWebExchange
    ): User = userRepository.getBySnowflake(exchange.awaitSession().attributes["SNOWFLAKE"] as Long)!!

    /**
     * Update own user data, allows changes to username, birthday, gender and birthday
     */
    @RequestMapping("/users/@me", method = [RequestMethod.POST])
    suspend fun updateSelf(
        exchange: ServerWebExchange
    ) {
        // Retrieve user and form data sent in
        val user = userRepository.getBySnowflake(exchange.awaitSession().attributes["SNOWFLAKE"] as Long)!!
        val form = exchange.awaitFormData().toSingleValueMap().toMap()

        val username = form["username"]
        val birthday = form["birthday"]
        val gender = form["gender"]
        val summary = form["summary"]

        // Perform some checks on constraints, makes sure that fields aren't empty
        // and makes sure that they are within set constraints (e.g. a maximum of
        // x amount of characters in the summary)
        if (username == null ||
            birthday == null ||
            gender == null ||
            summary == null
        ) { // TODO: Constrain these values
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return
        }

        // Update the user's data in the database with the form data
        userRepository.save(
            User(
                user.snowflake,
                user.email,
                username,
                LocalDate.parse(birthday),
                gender[0],
                summary
            )
        )

        // Send a NO CONTENT response, indicating that the operation was performed
        // but there is no response body and no further action to be taken.
        exchange.response.statusCode = HttpStatus.NO_CONTENT
    }
}