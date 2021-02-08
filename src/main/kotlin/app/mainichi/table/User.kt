package app.mainichi.table

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("users")
data class User(
    @Id
    val id: Long,
    val email: String,
    val username: String,
    val birthday: LocalDate?,
    val gender: Char?,
    val summary: String?,
    val avatar: String?,
    @JsonIgnore
    val password: String,
    @Version
    val version: Long
)
