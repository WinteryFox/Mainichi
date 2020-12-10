package app.mainichi.session

import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.server.WebSession
import org.springframework.web.server.session.WebSessionStore
import reactor.core.publisher.Mono
import java.time.Instant.now
import java.util.*
import java.util.UUID.fromString
import java.util.UUID.randomUUID
import java.util.concurrent.ConcurrentHashMap

@Component
class R2dbcWebSessionStore(
    @Value("\${SESSION_MAX_IDLE_TIME:-1}")
    private val maxIdleTime: Long,
    private val attributeService: AttributeService,
    private val repository: R2dbcWebSessionRepository
) : WebSessionStore {
    override fun createWebSession(): Mono<WebSession> = mono {
        val defaultAttributes = ConcurrentHashMap<String, Any>()
        val attributes = attributeService.toBlob(defaultAttributes)
        val now = now()

        val session = Session(
            randomUUID(),
            attributes,
            maxIdleTime,
            now,
            now,
            true,
            0
        )

        R2dbcWebSession(
            repository,
            session,
            false,
            defaultAttributes,
            attributeService
        )
    }

    override fun retrieveSession(sessionId: String): Mono<WebSession> = mono {
        if (sessionId == "null")
            return@mono null

        val session = repository.updateAndFindById(fromString(sessionId)) ?: return@mono null

        val attributes: MutableMap<String, Any> =
            attributeService.fromBlob(session.attributes)

        R2dbcWebSession(
            repository,
            session,
            true,
            attributes,
            attributeService
        )
    }

    override fun removeSession(sessionId: String): Mono<Void> = mono {
        repository.updateValidById(false, fromString(sessionId))
    }.then()

    override fun updateLastAccessTime(webSession: WebSession): Mono<WebSession> = mono {
        require(webSession is R2dbcWebSession) { "$webSession is not a RepositorySession" }
        webSession.updateLastAccessTime()
        webSession
    }
}