package app.mainichi.repository

import app.mainichi.objects.Language
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LanguageRepository : CoroutineCrudRepository<Language, String> {
    @Query("SELECT * FROM languages WHERE code = :code")
    suspend fun getByCode(code: String): Language?
}