package app.mainichi

import org.springframework.http.HttpStatus

const val BUCKET_NAME = "mainichi"
const val PROJECT_NAME = "mainichi"
const val AVATARS_LOCATION = "avatars"
const val MAX_AVATAR_SIZE = 256 * 1000 // 256 kB
const val MIN_AVATAR_WIDTH = 256
const val MAX_AVATAR_WIDTH = 256

enum class ErrorCode(
    val status: HttpStatus,
    val code: Short,
    val message: String
) {
    GENERAL_ERROR(HttpStatus.BAD_REQUEST, 0, "General error (such as a malformed request body, amongst other things)."),

    // 400
    INVALID_USERNAME(HttpStatus.BAD_REQUEST, 4001, "Invalid username."),
    INVALID_BIRTHDAY(HttpStatus.BAD_REQUEST, 4002, "Invalid date format, must be ISO-8601 format."),
    INVALID_GENDER(HttpStatus.BAD_REQUEST, 4003, "Invalid gender."),
    INVALID_SUMMARY(HttpStatus.BAD_REQUEST, 4004, "Invalid summary."),
    INVALID_AVATAR(HttpStatus.BAD_REQUEST, 4005, "Invalid avatar."),
}