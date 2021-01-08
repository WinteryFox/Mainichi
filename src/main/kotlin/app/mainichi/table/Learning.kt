package app.mainichi.table

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("learning")
data class Learning(
    @Id
    @JsonIgnore
    val snowflake: Long,
    val language: String,
    val proficiency: Short
)
