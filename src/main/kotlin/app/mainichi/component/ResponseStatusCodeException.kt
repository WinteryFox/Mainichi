package app.mainichi.component

import app.mainichi.ErrorCode
import org.springframework.web.server.ResponseStatusException

class ResponseStatusCodeException(
    val code: ErrorCode,
    cause: Throwable? = null
) : ResponseStatusException(
    code.status,
    code.message,
    cause
)