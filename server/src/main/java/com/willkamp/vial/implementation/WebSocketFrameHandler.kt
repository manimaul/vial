package com.willkamp.vial.implementation

import com.willkamp.vial.api.*
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*

internal class WebSocketFrameHandler(
        private val routeRegistry: RouteRegistry
) : SimpleChannelInboundHandler<WebSocketFrame>() {

    private val log = logger()
    private var meta: RouteRegistry.WSMeta? = null

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
        super.userEventTriggered(ctx, evt)
        log.info("user event = $evt")
        if (evt is WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("websocket handshake complete")
            meta = routeRegistry.findWebSocketHandler(evt.requestUri())
            Sender(ctx.channel()).also { sender ->
                meta?.init?.invoke(sender)
                ctx.channel().closeFuture().addListener {
                    log.info("websocket channel closed")
                    sender.channel = null
                }
            }
            ctx.pipeline()?.remove("request handler")
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: WebSocketFrame?) {
        when (msg) {
            is BinaryWebSocketFrame -> meta?.receiver?.invoke(WebSocketBinMessage(msg.content().array()))
            is TextWebSocketFrame -> meta?.receiver?.invoke(WebSocketTextMessage(msg.text()))
            is CloseWebSocketFrame -> meta?.receiver?.invoke(WebSocketClosed)
        }
    }

    private class Sender(
            var channel: Channel?
    ) : WebSocketSender {
        override fun send(webSocketMessage: WebSocketMessage) {
            when (webSocketMessage) {
                is WebSocketBinMessage -> {
                    channel?.writeAndFlush(BinaryWebSocketFrame(Unpooled.copiedBuffer(webSocketMessage.bin)))
                }
                WebSocketClosed -> {
                    channel?.close()
                }
                is WebSocketTextMessage -> {
                    channel?.writeAndFlush(TextWebSocketFrame(webSocketMessage.text))
                }
            }
        }

    }
}

