package app.mainichi.repository

import app.mainichi.table.Learning
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LearningRepository: CoroutineCrudRepository<Learning, String>