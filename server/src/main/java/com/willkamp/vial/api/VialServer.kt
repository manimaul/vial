package com.willkamp.vial.api

import com.willkamp.vial.implementation.Assembly
import io.netty.channel.ChannelHandler
import io.netty.handler.codec.http.HttpMethod
import java.io.Closeable
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

interface VialServer : ServerInitializer {
    companion object {

        @JvmStatic
        fun create(): VialServer {
            return Assembly.createVialServer()
        }

        fun customServer(channelInitializer: ChannelHandler) : ServerInitializer  {
            return Assembly.createCustomInitializer(channelInitializer)
        }
    }

    fun request(method: HttpMethod, route: String, handler: Consumer<Request>): VialServer

    fun addHandler(handler: EndPointHandler) = request(handler.method, handler.route, handler::handle)

    fun staticContent(rootDirectory: File): VialServer

    fun webSocket(route: String, senderReady: Consumer<WebSocket>) : VialServer

    fun httpOptions(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.OPTIONS, route, handler)
    }

    fun httpGet(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.GET, route, handler)
    }

    fun httpHead(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.HEAD, route, handler)
    }

    fun httpPost(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.POST, route, handler)
    }

    fun httpPut(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.PUT, route, handler)
    }

    fun httpPatch(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.PATCH, route, handler)
    }

    fun httpDelete(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.DELETE, route, handler)
    }

    fun httpTrace(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.TRACE, route, handler)
    }

    fun httpConnect(route: String, handler: Consumer<Request>): VialServer {
        return request(HttpMethod.CONNECT, route, handler)
    }
}

interface ServerInitializer: Closeable {
    fun listenAndServe(): CompletableFuture<Closeable>
    fun listenAndServeBlocking()
    val vialConfig: VialConfig
}
