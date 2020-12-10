package app.mainichi.repository

import app.mainichi.table.Like
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LikeRepository : CoroutineCrudRepository<Like, String>