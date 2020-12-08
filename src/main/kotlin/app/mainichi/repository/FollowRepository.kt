package app.mainichi.repository

import app.mainichi.objects.Follow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FollowRepository : CoroutineCrudRepository<Follow, String>