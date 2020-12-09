package app.mainichi.controller

import app.mainichi.data.Storage
import app.mainichi.objects.User
import app.mainichi.repository.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.reactive.server.awaitFormData
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.imageio.ImageIO

/**
 * REST controller for user data
 */
@RestController
class UserController(
    val userRepository: UserRepository,
    val storage: Storage
) {
    /**
     * Request own user data
     */
    @GetMapping("/users/@me", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getSelf(
        exchange: ServerWebExchange
    ): User = userRepository.findById(exchange.awaitSession().attributes["SNOWFLAKE"] as String)!!

    /**
     * Update own user data, allows changes to username, birthday, gender and birthday
     */
    @PostMapping("/users/@me", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun updateSelf(
        exchange: ServerWebExchange
    ): User? {
        // Retrieve user and form data sent in
        val user = userRepository.findById(exchange.awaitSession().attributes["SNOWFLAKE"] as String)!!
        val form = exchange.awaitFormData().toSingleValueMap().toMap()

        val username = form["username"]
        val birthday = form["birthday"]
        val gender = form["gender"]
        val summary = form["summary"]

        // Perform some checks on constraints, makes sure that fields aren't empty
        // and makes sure that they are within set constraints (e.g. a maximum of
        // x amount of characters in the summary)
        if (username == null ||
            username.length < 3 || username.length > 24 ||
            birthday == null ||
            gender == null ||
            !setOf("F", "M").contains(gender) ||
            summary == null ||
            summary.length < 32 || summary.length > 1024
        ) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        }

        // Update the user's data in the database with the form data
        try {
            return userRepository.save(
                User(
                    user.snowflake,
                    user.email,
                    username,
                    LocalDate.parse(birthday),
                    gender[0],
                    summary,
                    user.avatar
                )
            )
        } catch (exception: DateTimeParseException) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        }
    }

    /**
     * Update own avatar
     */
    @PatchMapping(
        "/users/@me",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun updateAvatar(
        exchange: ServerWebExchange,
        @RequestPart("avatar")
        part: FilePart
    ): User? {
        @Suppress("BlockingMethodInNonBlockingContext")
        val file = withContext(Dispatchers.IO) { Files.createTempFile(null, null) }.toFile()
        part.transferTo(file).awaitSingleOrNull()
        val user = userRepository.findById(exchange.awaitSession().attributes["SNOWFLAKE"] as String)!!

        try {
            // Test if the file being sent to us is actually an image and check its size
            if (file.length() > 256 * 1000)
                throw IOException()

            @Suppress("BlockingMethodInNonBlockingContext")
            withContext(Dispatchers.IO) {
                val image = ImageIO.read(file)

                if (image.width < 256 || image.width > 512 || // Image must be between 256x256 and 512x512 pixels
                    image.width != image.height                 // and it must be squar
                )
                    throw IOException()
            }

            // Save the image to the bucket and update the user's avatar in the database
            val updatedUser = userRepository.save(
                User(
                    user.snowflake,
                    user.email,
                    user.username,
                    user.birthday,
                    user.gender,
                    user.summary,
                    storage.putWithHash("avatars", file.readBytes(), MediaType.IMAGE_PNG_VALUE)
                )
            )

            // Delete the old avatar if it wasn't null and it isn't the same as the previous one
            if (user.avatar != null && updatedUser.avatar != user.avatar)
                storage.delete("avatars/${user.avatar}")

            return updatedUser
        } catch (exception: IOException) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        } finally {
            file.delete()
        }
    }
}