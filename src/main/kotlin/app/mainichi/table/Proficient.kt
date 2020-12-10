package app.mainichi.table

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("proficient")
data class Proficient(
    @Id
    val snowflake: Long,
    val language: String
)
