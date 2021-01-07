package app.mainichi.table

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("languages")
data class Language(
    @Id
    val code: String,
    val language: String
)
