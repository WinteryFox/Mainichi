package app.mainichi.request

data class UserLanguagesUpdateRequest(
    val learning: Set<String>,
    val proficient: Set<String>
)