package app.mainichi.service

import app.mainichi.event.Event
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service

@Service
class EventService {
    private val flow = MutableSharedFlow<ServerSentEvent<*>>()
    val publisher get() = flow.asSharedFlow()

    @Synchronized
    suspend fun <T : Event<*>> emit(payload: T) {
        flow.emit(
            ServerSentEvent.builder(payload)
                .build()
        )
    }
}
