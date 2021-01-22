package app.mainichi.`object`

data class UserLanguagesUpdateRequest(
    val learning: Set<String>,
    val proficient: Set<String>
)