package app.mainichi.table

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("proficient")
data class Proficient(
    @Id
    @JsonIgnore
    val id: Long,
    val language: String
)
