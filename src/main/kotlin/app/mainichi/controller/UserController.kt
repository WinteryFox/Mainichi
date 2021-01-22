package app.mainichi.controller

import app.mainichi.*
import app.mainichi.`object`.UserLanguagesUpdateRequest
import app.mainichi.`object`.UserUpdateRequest
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.data.Storage
import app.mainichi.data.toBuffer
import app.mainichi.repository.*
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
import org.springframework.http.codec.multipart.FilePart
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.server.awaitFormData
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
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
    val client: DatabaseClient,
    val storage: Storage
) {
    @GetMapping("/users/{ids}")
    suspend fun getUsers(
        exchange: ServerWebExchange,
        @PathVariable
        ids: Set<String>
    ) = userRepository.findAllById(ids)
        .map { PartialUser(it) }

    /**
     * Request own user data
     */
    @GetMapping("/users/@me", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getSelf(
        exchange: ServerWebExchange
    ): User {
        val id = exchange.awaitSession().attributes["id"] as String?
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        return userRepository.findById(id)!!
    }

    /**
     * Updates the proficient and the learning languages of the user
     */
    @PostMapping("/users/@me/languages")
    suspend fun updateLanguages(
        exchange: ServerWebExchange,
        @RequestBody
        request: UserLanguagesUpdateRequest
    ) {
        val user = userRepository.findById(exchange.awaitSession().attributes["id"] as String)!!

        learningRepository.deleteById(user.id.toString())
        proficientRepository.deleteById(user.id.toString())

        for (p in request.proficient)
            client.sql("INSERT INTO proficient (id, language) VALUES ($1, $2)")
                .bind(0, user.id)
                .bind(1, p)
                .await()

        for (l in request.learning)
            client.sql("INSERT INTO learning (id, language, proficiency) VALUES ($1, $2, 1)")
                .bind(0, user.id)
                .bind(1, l)
                .await()
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
        val user = userRepository.findById(exchange.awaitSession().attributes["id"] as String)!!

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
                    user.id,
                    user.email,
                    update.username,
                    if (update.birthday != null) LocalDate.parse(update.birthday) else null,
                    update.gender,
                    update.summary,
                    user.avatar,
                    user.version
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
    ): Resource {
        try {
            val blob: Blob = storage.get("avatars/$hash") ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

            exchange.response.headers.apply {
                this.cacheControl = CacheControl.maxAge(Duration.ofDays(365)).cachePublic().headerValue + ", immutable"
                this.expires = Instant.now().plus(Duration.ofDays(365)).toEpochMilli()
                this.lastModified = blob.updateTime
            }
            return ByteArrayResource(blob.toBuffer().array())
        } catch (exception: StorageException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
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
        val id = exchange.awaitSession().attributes["id"] as String?
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        val user = userRepository.findById(id)
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
                    user.id,
                    user.email,
                    user.username,
                    user.birthday,
                    user.gender,
                    user.summary,
                    storage.putWithHash(
                        AVATARS_LOCATION,
                        file.readBytes(),
                        MediaType.IMAGE_PNG_VALUE
                    ).name.substringAfter('/'),
                    user.version
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

    @GetMapping("/users/{id}/languages")
    suspend fun getUserLanguages(
        @PathVariable("id")
        id: String
    ): UserLanguages {
        val proficient = proficientRepository.findAllById(setOf(id))
            .map { it.language }
            .toSet()
        val learning = learningRepository.findAllById(setOf(id))
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
