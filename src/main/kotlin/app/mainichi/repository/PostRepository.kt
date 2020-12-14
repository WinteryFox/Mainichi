package app.mainichi.repository

import app.mainichi.table.Post
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepository : CoroutineCrudRepository<Post, String>