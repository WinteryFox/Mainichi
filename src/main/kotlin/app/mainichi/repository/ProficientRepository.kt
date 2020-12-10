package app.mainichi.repository

import app.mainichi.table.Proficient
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProficientRepository : CoroutineCrudRepository<Proficient, String>