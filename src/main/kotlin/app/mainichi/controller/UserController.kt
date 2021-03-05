package app.mainichi.controller

import app.mainichi.*
import app.mainichi.request.AvatarUploadRequest
import app.mainichi.request.UserLanguagesUpdateRequest
import app.mainichi.request.UserUpdateRequest
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.data.Storage
import app.mainichi.data.toBuffer
import app.mainichi.repository.*
import app.mainichi.table.*
import com.google.cloud.storage.Blob
import com.google.cloud.storage.StorageException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.apache.tika.parser.utils.DataURISchemeUtil
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import java.io.FileOutputStream
import java.nio.file.Files
import java.security.Principal
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.imageio.ImageIO

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
    @GetMapping("/users/{id}")
    suspend fun getUsers(
        @PathVariable
        id: String
    ) = userRepository.findById(id)

    /**
     * Request own user data
     */
    @GetMapping("/users/@me", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getSelf(
        principal: Principal
    ): User =
        userRepository.findById(principal.name)!!

    /**
     * Updates the proficient and the learning languages of the user
     */
    @PostMapping("/users/@me/languages")
    suspend fun updateLanguages(
        principal: Principal,
        @RequestBody
        request: UserLanguagesUpdateRequest
    ) {
        val user = userRepository.findById(principal.name)!!

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
        principal: Principal,
        @RequestBody
        update: UserUpdateRequest
    ): User {
        // Retrieve user and form data sent in
        val user = userRepository.findById(principal.name)!!

        if (update.username.isEmpty() ||
            update.username.length > MAX_USERNAME_LENGTH
        )
            throw ResponseStatusCodeException(ErrorCode.INVALID_USERNAME)

        if (update.gender != null && !setOf('F', 'M', null).contains(update.gender))
            throw ResponseStatusCodeException(ErrorCode.INVALID_GENDER)

        if (update.summary != null && (update.summary.isEmpty() || update.summary.length > MAX_SUMMARY_LENGTH))
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
                    user.password,
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
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun updateAvatar(
        principal: Principal,
        @RequestBody
        request: AvatarUploadRequest
    ): User {
        val user = userRepository.findById(principal.name)!!

        val dataUri = DataURISchemeUtil().parse(request.avatar)
        if (!dataUri.isBase64 || dataUri.mediaType != org.apache.tika.mime.MediaType.image("png"))
            throw ResponseStatusCodeException(ErrorCode.INVALID_AVATAR)

        @Suppress("BlockingMethodInNonBlockingContext")
        val path = withContext(Dispatchers.IO) {
            val temp = Files.createTempFile(null, null)

            dataUri.inputStream.use {
                FileOutputStream(temp.toFile()).write(it.readBytes())
            }

            return@withContext temp
        }
        val file = path.toFile()

        try {
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
                    user.password,
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
