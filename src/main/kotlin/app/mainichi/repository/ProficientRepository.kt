package app.mainichi.repository

import app.mainichi.objects.Proficient
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProficientRepository : CoroutineCrudRepository<Proficient, String>