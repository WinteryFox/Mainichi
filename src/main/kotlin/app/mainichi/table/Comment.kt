package app.mainichi.table

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("comments")
data class Comment(
    @Id
    val snowflake: Long,
    val post: Long,
    val commenter: Long,
    val content: String,
)
