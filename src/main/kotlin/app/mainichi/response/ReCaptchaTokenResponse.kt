package app.mainichi.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ReCaptchaTokenResponse(
    val success: Boolean,
    @JsonProperty("challenge_ts")
    val timestamp: LocalDateTime?,
    val hostname: String?,
    @JsonProperty("error-codes")
    val errorCodes: Set<String>?
)
