package app.mainichi.table

import org.springframework.data.relational.core.mapping.Table

@Table("followers")
data class Follow(
    val follower: Long,
    val followee: Long
)
