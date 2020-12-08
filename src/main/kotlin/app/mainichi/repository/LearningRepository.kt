package app.mainichi.repository

import app.mainichi.objects.Learning
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LearningRepository: CoroutineCrudRepository<Learning, String>