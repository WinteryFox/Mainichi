package app.mainichi.session

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface R2dbcWebSessionRepository : CoroutineCrudRepository<Session, UUID> {
    @Query(
        """
            UPDATE sessions
            SET valid = :valid, last_access_time = CURRENT_TIMESTAMP
            WHERE id = :id
        """
    )
    suspend fun updateValidById(valid: Boolean, id: UUID)

    @Query(
        """
            UPDATE sessions
            SET last_access_time = current_timestamp
            WHERE id = :id
            RETURNING *
        """
    )
    suspend fun updateAndFindById(id: UUID): Session?
}