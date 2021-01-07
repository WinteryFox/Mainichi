package app.mainichi.table

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("learning")
data class Learning(
    @Id
    val snowflake: Long,
    val language: String,
    val proficiency: Short
)
