package app.mainichi.repository

import app.mainichi.table.Like
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository


interface LikeRepository : CoroutineCrudRepository<Like, String> {
    suspend fun findAllByLiker(id: Long): Flow<Like>

    suspend fun findAllByPost(id: Long): Flow<Like>
}
