package app.mainichi.table

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table

@Table("posts")
data class Post(
    @Id
    val id: Long,
    val author: Long,
    val content: String,
    @Version
    val version: Long
)

