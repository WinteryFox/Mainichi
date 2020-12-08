package app.mainichi.repository

import app.mainichi.objects.Post
import kotlinx.coroutines.flow.Flow
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepository : CoroutineCrudRepository<Post, String> {
    @Query
    suspend fun findAllByAuthor(userId: Long): Flow<Post>
}