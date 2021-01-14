package app.mainichi.`object`

data class UserLanguagesUpdateRequest(
    val learning: List<String>,
    val proficient: List<String>
)