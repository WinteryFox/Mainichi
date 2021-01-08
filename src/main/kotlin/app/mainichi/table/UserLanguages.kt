package app.mainichi.table

data class UserLanguages(
    val proficient: Set<String>,
    val learning: Set<Learning>
)
