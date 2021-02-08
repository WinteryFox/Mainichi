package app.mainichi.request

data class UserUpdateRequest(
    val username: String,
    val gender: Char?,
    val birthday: String?,
    val summary: String?
)
