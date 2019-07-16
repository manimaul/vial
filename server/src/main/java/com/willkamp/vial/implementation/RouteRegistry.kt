package com.willkamp.vial.implementation

import com.willkamp.vial.api.RequestHandler
import io.netty.handler.codec.http.HttpMethod
import java.util.*

internal class RouteRegistry {
    private val routeHandlers = HashMap<HttpMethod, MutableList<Meta>>()

    fun registerRoute(method: HttpMethod,
                      routePattern: String,
                      handler: RequestHandler) {
        routeHandlers.computeIfAbsent(method) {
            mutableListOf()

        }.add(Meta(Route.build(routePattern), handler))
    }

    fun findHandler(method: CharSequence, path: CharSequence): Optional<Meta> {
        return findHandler(HttpMethod.valueOf(method.toString()), path)
    }

    fun findHandler(method: HttpMethod, path: CharSequence): Optional<Meta> {
        return Optional.ofNullable(routeHandlers[method])
                .flatMap { metaList ->
                    metaList.stream().filter { meta ->
                        meta.route?.matches(path) ?: false
                    }.findFirst()
                }
    }

    internal data class Meta(
            val route: Route? = null,
            val handler: RequestHandler
    )
}
