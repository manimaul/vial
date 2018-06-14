package com.willkamp.vial.api

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.internal.VialServerImpl
import kotlinx.coroutines.experimental.async
import java.io.Closeable
import java.util.concurrent.CompletableFuture

class VialServer(
        var port: Int = 8080,
        var minimumProtocol: Protocol = Protocol.HTTP_1_1,
        var tlsContext: TlsContext? = null
) {

    private val handlers = mutableMapOf<String, RequestHandler>()

    fun get(route: String, handler: RequestHandler): VialServer {
        handlers["GET_$route"] = handler
        return this
    }

    fun post(route: String, handler: RequestHandler): VialServer {
        handlers["POST_$route"] = handler
        return this
    }

    //todo: other methods

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