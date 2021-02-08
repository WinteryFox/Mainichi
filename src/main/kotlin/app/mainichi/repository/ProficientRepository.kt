package app.mainichi.repository

import app.mainichi.table.Proficient
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProficientRepository : CoroutineCrudRepository<Proficient, String>{
    suspend fun findAllByid(id: Long): Flow<Proficient>
}