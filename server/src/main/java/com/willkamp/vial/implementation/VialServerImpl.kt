package com.willkamp.vial.implementation

import com.willkamp.vial.api.*
import io.netty.handler.codec.http.HttpMethod
import java.io.File
import java.util.function.Consumer

internal class VialServerImpl internal constructor(
        vialConfig: VialConfig,
        channelConfig: ChannelConfig,
        vialChannelInitializer: VialChannelInitializer,
        private val routeRegistry: RouteRegistry) : VialServer, NettyInitializer(vialChannelInitializer, channelConfig, vialConfig) {
    private val log = logger()

    override fun request(method: HttpMethod, route: String, handler: Consumer<Request>): VialServer {
        routeRegistry.registerRoute(method, route, handler)
        return this
    }

    override fun staticContent(rootDirectory: File): VialServer {
        log.error("static content noop - not implemented")
        return this
    }

    override fun webSocket(
            route: String,
            senderReady: Consumer<WebSocket>) : VialServer {
        routeRegistry.registerWebSocketRoute(route, senderReady)
        return this
    }
}
