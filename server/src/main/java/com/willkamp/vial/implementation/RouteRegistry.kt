package com.willkamp.vial.implementation

import com.willkamp.vial.api.*
import io.netty.handler.codec.http.HttpMethod
import java.util.*
import java.util.function.Consumer

internal class RouteRegistry {
    private val routeHandlers = HashMap<HttpMethod, MutableList<Meta>>()
    private val wsHandlers = mutableListOf<WSMeta>()
    private val wsBaseRoutes = mutableSetOf<String>()
    private val log = logger()

    fun registerRoute(method: HttpMethod,
                      routePattern: String,
                      handler: Consumer<Request>) {
        routeHandlers.computeIfAbsent(method) {
            mutableListOf()
        }.add(Meta(Route.build(routePattern), handler))
    }

    fun findHandler(method: CharSequence, url: CharSequence): Optional<Meta> {
        return findHandler(HttpMethod.valueOf(method.toString()), url)
    }

    fun findHandler(method: HttpMethod, url: CharSequence): Optional<Meta> {
        return Optional.ofNullable(routeHandlers[method])
                .flatMap { metaList ->
                    metaList.stream().filter { meta ->
                        meta.route?.matches(url.stripParams()) ?: false
                    }.findFirst()
                }
    }

    fun baseWsRoutes() = wsBaseRoutes

    fun registerWebSocketRoute(route: String, senderReady: Consumer<WebSocket>) {
        var add = true
        wsBaseRoutes.iterator().also { itor ->
            while (itor.hasNext()) {
                val handler = itor.next()
                if (handler.startsWith(route)) {
                    itor.remove()
                } else if (route.startsWith(handler)) {
                    add = false
                }
            }
        }
        if (add) {
            wsBaseRoutes.add(route)
        }
        wsHandlers.add(WSMeta(route, senderReady))
    }

    fun findWebSocketHandler(route: String) : WSMeta? {
        log.info("finding websocket handler for route $route")
        return wsHandlers.firstOrNull {
            it.route == route.stripParams()
        }
    }

    internal data class Meta(
            val route: Route? = null,
            val handler: Consumer<Request>,
    )

    internal data class WSMeta(
            val route: String,
            val init: Consumer<WebSocket>,
    )

    private fun CharSequence.stripParams() : CharSequence = indexOf('?').takeIf { it > 0 }?.let { subSequence(0, it) } ?: this
}
