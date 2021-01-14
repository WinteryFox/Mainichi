package app.mainichi.repository

import app.mainichi.table.Like
import kotlinx.coroutines.flow.Flow
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository


interface LikeRepository : CoroutineCrudRepository<Like, String> {
    @Query
    suspend fun findAllByLiker(snowflake: Long): Flow<Like>

    @Query
    suspend fun findAllByPost(snowflake: Long): Flow<Like>
}
