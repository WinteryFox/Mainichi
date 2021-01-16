package app.mainichi.controller

import app.mainichi.service.EventService
import kotlinx.coroutines.flow.SharedFlow
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EventController(
    val service: EventService
) {
    @GetMapping(
        "/events",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun events(): SharedFlow<ServerSentEvent<*>> = service.publisher
}