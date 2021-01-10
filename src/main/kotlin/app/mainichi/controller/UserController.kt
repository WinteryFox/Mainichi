package app.mainichi.controller

import app.mainichi.*
import app.mainichi.`object`.UserUpdateRequest
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.data.Storage
import app.mainichi.data.toBuffer
import app.mainichi.repository.LanguageRepository
import app.mainichi.repository.LearningRepository
import app.mainichi.repository.ProficientRepository
import app.mainichi.repository.UserRepository
import app.mainichi.table.*
import com.google.cloud.storage.Blob
import com.google.cloud.storage.StorageException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.server.awaitFormData
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.imageio.ImageIO
import kotlin.streams.toList

/**
 * REST controller for user data
 */
@RestController
class UserController(
    val userRepository: UserRepository,
    val proficientRepository: ProficientRepository,
    val learningRepository: LearningRepository,
    val languageRepository: LanguageRepository,
    val storage: Storage
) {
    @GetMapping("/users/{snowflakes}")
    suspend fun getUsers(
        exchange: ServerWebExchange,
        @PathVariable
        snowflakes: Set<String>
    ) = userRepository.findAllById(snowflakes)
        .map { PartialUser(it) }

    /**
     * Request own user data
     */
    @GetMapping("/users/@me", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getSelf(
        exchange: ServerWebExchange
    ): User? {
        val snowflake = exchange.awaitSession().attributes["SNOWFLAKE"] as String?
        if (snowflake == null) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return null
        }

        return userRepository.findById(snowflake)!!
    }

    /**
     * Updates the proficient and the learning languages of the user
     */
    @PostMapping("/users/@me/languages", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    suspend fun updateLanguages(
        exchange: ServerWebExchange
    ) {
        val user = userRepository.findById(exchange.awaitSession().attributes["SNOWFLAKE"] as String)!!
        val form = exchange.awaitFormData()

        val learning = form["learning"]
        val proficient = form["proficient"]

        //check if keys are present, if not return
        if (learning == null || learning.size == 0 || proficient == null || proficient.size == 0) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return
        }

        learningRepository.saveAll(learning.stream().map { Learning(user.snowflake, it, 1) }.toList())
            .collect() //TODO change proficiency
        proficientRepository.saveAll(proficient.stream().map { Proficient(user.snowflake, it) }.toList()).collect()
    }

    /**
     * Update own user data, allows changes to username, birthday, gender and birthday
     */
    @PostMapping(
        "/users/@me",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun updateSelf(
        exchange: ServerWebExchange,
        @RequestBody
        update: UserUpdateRequest
    ): User {
        // Retrieve user and form data sent in
        val user = userRepository.findById(exchange.awaitSession().attributes["SNOWFLAKE"] as String)!!

        if (update.username.isEmpty() ||
            update.username.length > 16
        )
            throw ResponseStatusCodeException(ErrorCode.INVALID_USERNAME)

        if (update.gender != null && !setOf('F', 'M', null).contains(update.gender))
            throw ResponseStatusCodeException(ErrorCode.INVALID_GENDER)

        if (update.summary != null && (update.summary.isEmpty() || update.summary.length > 2048))
            throw ResponseStatusCodeException(ErrorCode.INVALID_SUMMARY)

        // Update the user's data in the database with the form data
        try {
            return userRepository.save(
                User(
                    user.snowflake,
                    user.email,
                    update.username,
                    if (update.birthday != null) LocalDate.parse(update.birthday) else null,
                    update.gender,
                    update.summary,
                    user.avatar
                )
            )
        } catch (exception: DateTimeParseException) {
            throw ResponseStatusCodeException(ErrorCode.INVALID_BIRTHDAY)
        }
    }

    @GetMapping("/avatars/{hash}.png", produces = [MediaType.IMAGE_PNG_VALUE])
    suspend fun getAvatar(
        exchange: ServerWebExchange,
        @PathVariable("hash")
        hash: String
    ): Resource? {
        try {
            val blob: Blob? = storage.get("avatars/$hash")
            if (blob == null) {
                exchange.response.statusCode = HttpStatus.BAD_REQUEST
                return null
            }

            exchange.response.headers.apply {
                this.cacheControl = CacheControl.maxAge(Duration.ofDays(365)).cachePublic().headerValue + ", immutable"
                this.expires = Instant.now().plus(Duration.ofDays(365)).toEpochMilli()
                this.lastModified = blob.updateTime
            }
            return ByteArrayResource(blob.toBuffer().array())
        } catch (exception: StorageException) {
            exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
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
    ): User {
        val snowflake = exchange.awaitSession().attributes["SNOWFLAKE"] as String?
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        val user = userRepository.findById(snowflake)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        @Suppress("BlockingMethodInNonBlockingContext")
        val path = withContext(Dispatchers.IO) { Files.createTempFile(null, null) }
        val file = path.toFile()
        try {
            part.transferTo(file).awaitSingleOrNull()

            if (file.length() > MAX_AVATAR_SIZE)
                throw ResponseStatusCodeException(ErrorCode.INVALID_AVATAR)

            @Suppress("BlockingMethodInNonBlockingContext")
            val type = withContext(Dispatchers.IO) { Files.probeContentType(path) }
            if (type != MediaType.IMAGE_PNG_VALUE)
                throw ResponseStatusCodeException(ErrorCode.INVALID_AVATAR)

            @Suppress("BlockingMethodInNonBlockingContext")
            val image = withContext(Dispatchers.IO) { ImageIO.read(file) }
            if (image.width < MIN_AVATAR_WIDTH ||
                image.height > MAX_AVATAR_WIDTH ||
                image.width != image.height
            )
                throw ResponseStatusCodeException(ErrorCode.INVALID_AVATAR)

            // Save the image to the bucket and update the user's avatar in the database
            val updatedUser = userRepository.save(
                User(
                    user.snowflake,
                    user.email,
                    user.username,
                    user.birthday,
                    user.gender,
                    user.summary,
                    storage.putWithHash(
                        AVATARS_LOCATION,
                        file.readBytes(),
                        MediaType.IMAGE_PNG_VALUE
                    ).name.substringAfter('/')
                )
            )

            // Delete the old avatar if it wasn't null and it isn't the same as the previous one
            if (user.avatar != null && updatedUser.avatar != user.avatar)
                storage.delete("$AVATARS_LOCATION/${user.avatar}")

            return updatedUser
        } finally {
            file.delete()
        }
    }

    @GetMapping("/users/{snowflake}/languages")
    suspend fun getUserLanguages(
        @PathVariable("snowflake")
        snowflake: String
    ): UserLanguages {
        val proficient = proficientRepository.findAllById(setOf(snowflake))
            .map { it.language }
            .toSet()
        val learning = learningRepository.findAllById(setOf(snowflake))
            .toSet()

        return UserLanguages(
            proficient,
            learning
        )
    }

    @GetMapping("/languages")
    suspend fun getLanguages(): Flow<Language> =
        languageRepository.findAll()
}