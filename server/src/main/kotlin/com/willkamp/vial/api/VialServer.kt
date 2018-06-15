package com.willkamp.vial.api

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.internal.VialServerImpl
import io.netty.handler.codec.http.HttpMethod
import kotlinx.coroutines.experimental.async
import java.io.Closeable
import java.util.concurrent.CompletableFuture

class VialServer(
        var port: Int = 8080,
        var minimumProtocol: Protocol = Protocol.HTTP_1_1,
        var tlsContext: TlsContext? = null
) {

    private val handlers = mutableMapOf<String, RequestHandler>()

    fun request(method: HttpMethod, route: String, handler: RequestHandler): VialServer {
        handlers["${method}_$route"] = handler
        return this
    }

    fun options(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.OPTIONS, route, handler)
    }

    fun get(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.GET, route, handler)
    }

    fun head(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.HEAD, route, handler)
    }

    fun post(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.POST, route, handler)
    }

    fun put(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.PUT, route, handler)
    }

    fun patch(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.PATCH, route, handler)
    }

    fun delete(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.DELETE, route, handler)
    }

    fun trace(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.TRACE, route, handler)
    }

    fun connect(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.CONNECT, route, handler)
    }

    fun buildAndServe() {
        build().serve()
    }

    fun buildAndServeAsync(): CompletableFuture<Closeable> {
        val server = build()
        val future = CompletableFuture<Closeable>()
        async { server.serve(future) }
        return future
    }

    private fun build(): VialServerImpl {
        if (Protocol.HTTP_2 == minimumProtocol && tlsContext == null) {
            throw RuntimeException("tls context required for http/2")
        }
        return VialServerImpl(port, Protocol.HTTP_2 == minimumProtocol, tlsContext, ImmutableMap.copyOf(handlers))
    }
}