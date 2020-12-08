package app.mainichi.repository

import app.mainichi.objects.Language
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LanguageRepository : CoroutineCrudRepository<Language, String>