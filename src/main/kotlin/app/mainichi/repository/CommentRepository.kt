package app.mainichi.repository

import app.mainichi.table.Comment
import kotlinx.coroutines.flow.Flow
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CommentRepository: CoroutineCrudRepository<Comment, String> {
    @Query
    suspend fun findAllByPost(post: Long): Flow<Comment>
}