package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table
data class Follow(
    val follower: Long,
    val followee: Long
)
