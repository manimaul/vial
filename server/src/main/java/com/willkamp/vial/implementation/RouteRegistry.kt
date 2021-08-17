package com.willkamp.vial.implementation

import com.willkamp.vial.api.*
import io.netty.handler.codec.http.HttpMethod
import java.util.*

internal class RouteRegistry {
    private val routeHandlers = HashMap<HttpMethod, MutableList<Meta>>()
    val wsHandlers = mutableListOf<WSMeta>()
    private val log = logger()

    fun registerRoute(method: HttpMethod,
                      routePattern: String,
                      handler: RequestHandler) {
        routeHandlers.computeIfAbsent(method) {
            mutableListOf()
        }.add(Meta(Route.build(routePattern), handler))
    }

    fun findHandler(method: CharSequence, url: CharSequence): Optional<Meta> {
        return findHandler(HttpMethod.valueOf(method.toString()), url)
    }

    fun findHandler(method: HttpMethod, url: CharSequence): Optional<Meta> {
        val path = url.indexOf('?').takeIf { it > 0 }?.let { url.subSequence(0, it) } ?: url
        return Optional.ofNullable(routeHandlers[method])
                .flatMap { metaList ->
                    metaList.stream().filter { meta ->
                        meta.route?.matches(path) ?: false
                    }.findFirst()
                }
    }

    fun registerWebSocketRoute(route: String, senderReady: WebSocketHandlerInit, receiver: WebSocketReceiver) {
        wsHandlers.add(WSMeta(route, receiver, senderReady))
    }

    fun findWebSocketHandler(route: String) : WSMeta? {
        log.info("finding websocket handler for route $route")
        return wsHandlers.firstOrNull {
            it.route == route
        }
    }

    internal data class Meta(
            val route: Route? = null,
            val handler: RequestHandler,
    )

    internal data class WSMeta(
            val route: String,
            val receiver: WebSocketReceiver,
            val init: WebSocketHandlerInit,
    )
}
