package app.mainichi.repository

import app.mainichi.table.Follow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FollowRepository : CoroutineCrudRepository<Follow, String>