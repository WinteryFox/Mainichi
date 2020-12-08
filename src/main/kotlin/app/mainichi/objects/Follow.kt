package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table("followers")
data class Follow(
    val follower: Long,
    val followee: Long
)
