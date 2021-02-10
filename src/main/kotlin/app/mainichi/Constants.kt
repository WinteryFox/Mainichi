package app.mainichi

import org.springframework.http.HttpStatus

const val BUCKET_NAME = "mainichi"
const val AVATARS_LOCATION = "avatars"
const val MAX_AVATAR_SIZE = 256 * 1000 // 256 kB
const val MIN_AVATAR_WIDTH = 256
const val MAX_AVATAR_WIDTH = 256
const val MAX_USERNAME_LENGTH = 32
const val MAX_SUMMARY_LENGTH = 2048

enum class ErrorCode(
    val status: HttpStatus,
    val code: Short,
    val message: String
) {
    // 400
    INVALID_USERNAME(
        HttpStatus.BAD_REQUEST,
        4001,
        "Invalid username."
    ),
    INVALID_BIRTHDAY(
        HttpStatus.BAD_REQUEST,
        4002,
        "Invalid date format, must be ISO-8601 format."
    ),
    INVALID_GENDER(
        HttpStatus.BAD_REQUEST,
        4003,
        "Invalid gender."
    ),
    INVALID_SUMMARY(
        HttpStatus.BAD_REQUEST,
        4004,
        "Invalid summary."
    ),
    INVALID_AVATAR(
        HttpStatus.BAD_REQUEST,
        4005,
        "Invalid avatar."
    ),
    FAILED_CAPTCHA(
        HttpStatus.BAD_REQUEST,
        4006,
        "The captcha was solved incorrectly."
    ),
    USER_EXISTS(
        HttpStatus.BAD_REQUEST,
        4007,
        "That email is already registered to another account."
    ),
    INVALID_COMMENT(
        HttpStatus.BAD_REQUEST,
        4008,
        "Comments may not be empty and may not be longer than 1024 characters."
    ),
    INVALID_POST(
        HttpStatus.BAD_REQUEST,
        4009,
        "Posts may not be empty and may not be longer than 2048 characters."
    ),
}