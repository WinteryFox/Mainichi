package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table
data class Like(
    val post: Long,
    val liker: Long
)
