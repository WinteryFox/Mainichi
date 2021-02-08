package app.mainichi.request

data class ReCaptchaTokenRequest(
    val secret: String,
    val response: String
)
