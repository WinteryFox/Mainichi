package app.mainichi.repository

import app.mainichi.objects.Follow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FollowRepository : CoroutineCrudRepository<Follow, String> {
    @Query("SELECT * FROM likes WHERE followee = :followee")
    suspend fun getByFollowee(code: String): List<Follow>
}