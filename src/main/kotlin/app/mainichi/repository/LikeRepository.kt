package app.mainichi.repository

import app.mainichi.objects.Like
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LikeRepository : CoroutineCrudRepository<Like, String>