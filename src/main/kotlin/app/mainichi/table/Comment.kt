package app.mainichi.table

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table

@Table("comments")
data class Comment(
    @Id
    val id: Long,
    val post: Long,
    val commenter: Long,
    val content: String,
    @Version
    val version: Long
)
