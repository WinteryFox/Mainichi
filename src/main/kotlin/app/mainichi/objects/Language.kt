package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table("languages")
data class Language(
    val code: String,
    val language: String,
)
