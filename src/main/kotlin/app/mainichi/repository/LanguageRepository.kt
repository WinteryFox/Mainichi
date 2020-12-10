package app.mainichi.repository

import app.mainichi.table.Language
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LanguageRepository : CoroutineCrudRepository<Language, String>