package app.mainichi.component

import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException

@Suppress("LeakingThis")
@Primary
@Component
@Order(HIGHEST_PRECEDENCE)
class WebExceptionHandler(
    errorAttributes: ErrorAttributes,
    resourceProperties: WebProperties.Resources,
    applicationContext: ApplicationContext,
    configurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(
    errorAttributes,
    resourceProperties,
    applicationContext
) {
    init {
        setMessageWriters(configurer.writers)
    }

    override fun getRoutingFunction(
        errorAttributes: ErrorAttributes
    ): RouterFunction<ServerResponse> = RouterFunctions.route(RequestPredicates.all()) {
        val error = errorAttributes.getError(it)
        val attributes = errorAttributes.getErrorAttributes(it, ErrorAttributeOptions.defaults())

        return@route when (error) {
            is ResponseStatusCodeException -> {
                attributes["code"] = error.code.code
                attributes["message"] = error.code.message
                ServerResponse.status(error.code.status)
                    .bodyValue(attributes)
            }
            else -> ServerResponse.status(attributes["status"] as Int)
                .bodyValue(attributes)
        }
    }
}
