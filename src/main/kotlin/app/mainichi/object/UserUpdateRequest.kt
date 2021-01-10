package app.mainichi.`object`

data class UserUpdateRequest(
    val username: String,
    val gender: Char?,
    val birthday: String?,
    val summary: String?
)
