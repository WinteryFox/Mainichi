package app.mainichi.repository

import app.mainichi.objects.Comment
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CommentRepository: CoroutineCrudRepository<Comment, String>