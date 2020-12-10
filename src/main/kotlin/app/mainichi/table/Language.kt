package app.mainichi.table

import org.springframework.data.relational.core.mapping.Table

@Table("languages")
data class Language(
    val code: String,
    val language: String,
)
