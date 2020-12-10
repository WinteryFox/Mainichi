package app.mainichi.table

import org.springframework.data.relational.core.mapping.Table

@Table("learning")
data class Learning(
    val snowflake: Long,
    val language: String,
    val profiency: Short
)
