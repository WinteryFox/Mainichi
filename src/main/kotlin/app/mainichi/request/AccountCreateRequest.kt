package app.mainichi.request

import java.time.LocalDate

data class AccountCreateRequest(
    val captcha: String,
    val gender: Char?,
    val email: String,
    val password: String,
    val summary: String,
    val username: String,
    val birthday: LocalDate
)