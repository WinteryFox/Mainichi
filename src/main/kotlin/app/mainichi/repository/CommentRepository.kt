package app.mainichi.repository

import app.mainichi.table.Comment
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CommentRepository: CoroutineCrudRepository<Comment, String>