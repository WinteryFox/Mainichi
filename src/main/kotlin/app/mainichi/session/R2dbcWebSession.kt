package app.mainichi.session

import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.time.Duration.ofNanos
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.Delegates

class R2dbcWebSession(
    private val repository: R2dbcWebSessionRepository,
    session: Session,
    started: Boolean,
    private val attributes: MutableMap<String, Any>,
    private val attributeService: AttributeService
) : WebSession {
    private val sessionData = AtomicReference(session to started)
    private val session get() = sessionData.get().first

    var id: Long? = null

    override fun getId(): String = session.id.toString()

    override fun getAttributes(): MutableMap<String, Any> = attributes

    override fun start() = sessionData.updateAndGet { (session) -> session to true }.run { }

    override fun isStarted(): Boolean =
        sessionData.updateAndGet { (session, started) -> session to (started || attributes.isNotEmpty()) }.second

    override fun changeSessionId(): Mono<Void> = mono {
        save { repository.save(session) }
    }.onErrorResume().then()

    override fun invalidate(): Mono<Void> = mono {
        sessionData.updateAndGet { (session, started) ->
            session.copy(isValid = false) to started
        }

        save { repository.save(it) }
    }.onErrorResume().then()

    override fun save(): Mono<Void> = mono {
        save { repository.save(it) }
    }.then()

    override fun isExpired(): Boolean {
        val (session) = sessionData.updateAndGet { (session, started) ->
            val maxIdleTime = ofNanos(session.maxIdleTime)
            val expired = !maxIdleTime.isNegative && ((now() - maxIdleTime) > session.lastAccessTime)

            session.copy(isValid = session.isValid && !expired) to started
        }

        return !session.isValid
    }

    override fun getCreationTime(): Instant = session.creationTime

    override fun getLastAccessTime(): Instant = session.lastAccessTime

    override fun setMaxIdleTime(maxIdleTime: Duration) =
        sessionData.updateAndGet { (session, started) ->
            session.copy(maxIdleTime = maxIdleTime.toNanos()) to started
        }.run {}

    override fun getMaxIdleTime(): Duration = ofNanos(session.maxIdleTime)

    private suspend fun save(block: suspend (Session) -> Session) = mono {
        val oldSessionData = sessionData.updateAndGet { (session, started) ->
            val attributesCopy = ConcurrentHashMap(attributes)
            val now = now()

            val maxIdleTime = ofNanos(session.maxIdleTime)
            val expired = !maxIdleTime.isNegative && ((now - maxIdleTime) > session.lastAccessTime)

            session.copy(
                attributes = attributeService.toBlob(attributesCopy),
                lastAccessTime = now,
                isValid = session.isValid && !expired
            ) to (started || attributesCopy.isNotEmpty())
        }

        val (oldSession, oldStarted) = oldSessionData
        if (oldStarted) {
            val newSessionData = block(oldSession) to oldStarted
            val (newSession) = newSessionData

            if (!sessionData.compareAndSet(oldSessionData, newSessionData)) {
                throw ConcurrentModificationException("$oldSessionData -> $newSessionData")
            } else if (!newSession.isValid) {
                throw InvalidSessionException(newSession.toString())
            }
        }
    }.retryWhen(Retry.indefinitely()
        .filter { it is OptimisticLockingFailureException }
        .doAfterRetryAsync { update(it.failure()) }
    ).awaitLast()

    private fun update(cause: Throwable): Mono<Void> = mono {
        val session = repository.findById(session.id) ?: throw cause
        sessionData.set(session to true)
    }.then()

    suspend fun updateLastAccessTime(): Unit = mono {
        save { repository.save(it) }
    }.onErrorResume().awaitLast()

    private companion object {
        fun <T> Mono<T>.onErrorResume(): Mono<T> =
            onErrorResume(InvalidSessionException::class.java) {
                // TODO: Add log to indicate session invalidation
                Mono.empty()
            }
    }
}