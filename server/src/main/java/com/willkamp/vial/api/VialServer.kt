package com.willkamp.vial.api

import com.willkamp.vial.implementation.Assembly
import io.netty.handler.codec.http.HttpMethod
import java.io.Closeable
import java.io.File
import java.util.concurrent.CompletableFuture

interface VialServer {

    fun request(method: HttpMethod, route: String, handler: RequestHandler): VialServer

    fun staticContent(rootDirectory: File): VialServer

    fun listenAndServeBlocking()

    fun listenAndServe(): CompletableFuture<Closeable>

    fun httpOptions(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.OPTIONS, route, handler)
    }

    fun httpGet(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.GET, route, handler)
    }

    fun httpHead(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.HEAD, route, handler)
    }

    fun httpPost(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.POST, route, handler)
    }

    fun httpPut(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.PUT, route, handler)
    }

    fun httpPatch(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.PATCH, route, handler)
    }

    fun httpDelete(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.DELETE, route, handler)
    }

    fun httpTrace(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.TRACE, route, handler)
    }

    fun httpConnect(route: String, handler: RequestHandler): VialServer {
        return request(HttpMethod.CONNECT, route, handler)
    }

    companion object {

        fun create(): VialServer {
            return Assembly.vialServer
        }
    }
}
