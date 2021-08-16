package com.willkamp.vial.implementation

import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.handler.ssl.SslContext

internal class VialChannelInitializer(
        private val sslContext: SslContext?,
        private val vialConfig: VialConfig,
        private val routeRegistry: RouteRegistry
) : ChannelInitializer<SocketChannel>() {

    @Throws(Exception::class)
    override fun initChannel(ch: SocketChannel) {
        sslContext?.let {
            configureH2(ch, it)
        } ?: run {
            configureH1(ch.pipeline())
        }
    }

    private fun configureH2(ch: SocketChannel, sslContext: SslContext) {
        ch.pipeline().addLast(
                sslContext.newHandler(ch.alloc()),
                AlpnHandler(
                        fallback = { this.configureH1(it) },
                        routeRegistry = routeRegistry,
                        vialConfig = vialConfig
                )
        )
    }

    private fun configureH1(pipeline: ChannelPipeline) {
        pipeline
                .addLast("server codec duplex", HttpServerCodec())
                .addLast("message size limit aggregator", HttpObjectAggregator(vialConfig.maxContentLength))
                .addLast("websocket compression", WebSocketServerCompressionHandler())

        routeRegistry.wsHandlers.forEach {
            pipeline.addLast(WebSocketServerProtocolHandler(it.route, null, true, vialConfig.maxContentLength))
        }

        pipeline
                .addLast("websocket frames", WebSocketFrameHandler(routeRegistry))
                .addLast("request handler", H1BrokerHandler(routeRegistry))
    }
}
