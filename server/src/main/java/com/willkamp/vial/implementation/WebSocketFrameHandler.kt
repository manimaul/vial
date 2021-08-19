package com.willkamp.vial.implementation

import com.willkamp.vial.api.*
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*
import io.netty.util.AttributeKey
import java.util.function.Consumer

private val webSocketKey = AttributeKey.valueOf<WebSocketImpl>("webSocketKey")

internal class WebSocketFrameHandler(
        private val routeRegistry: RouteRegistry
) : SimpleChannelInboundHandler<WebSocketFrame>() {

    private val log = logger()

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
        super.userEventTriggered(ctx, evt)
        log.info("user event = $evt")
        if (evt is WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("websocket handshake complete")
            routeRegistry.findWebSocketHandler(evt.requestUri())?.let { meta ->
                ctx.channel().attr(webSocketKey).set(
                        WebSocketImpl(ctx.channel()).also { sender ->
                            meta.init.accept(sender)
                        }
                )
            }
            ctx.pipeline()?.remove("request handler")
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: WebSocketFrame?) {
        ctx?.channel()?.attr(webSocketKey)?.get()?.let { webSocketImpl ->
            when (msg) {
                is BinaryWebSocketFrame -> webSocketImpl.accept(WebSocketBinMessage(msg.content().array()))
                is TextWebSocketFrame -> webSocketImpl.accept(WebSocketTextMessage(msg.text()))
                is CloseWebSocketFrame -> webSocketImpl.accept(WebSocketClosed)
                else -> Unit
            }
        }
    }


}

private class WebSocketImpl(
        var channel: Channel?
) : WebSocket {

    val log = logger()
    var receivers = mutableListOf<Consumer<WebSocketMessage>>()

    init {
        channel?.closeFuture()?.addListener {
            log.info("websocket channel closed")
            channel = null
            receivers.clear()
        }
    }

    fun accept(message: WebSocketMessage) {
        receivers.forEach {
            it.accept(message)
        }
    }
    override fun send(message: WebSocketMessage) {
        when (message) {
            is WebSocketBinMessage -> {
                channel?.writeAndFlush(BinaryWebSocketFrame(Unpooled.copiedBuffer(message.bin)))
            }
            WebSocketClosed -> {
                channel?.close()
            }
            is WebSocketTextMessage -> {
                channel?.writeAndFlush(TextWebSocketFrame(message.text))
            }
        }
    }

    override fun receive(receiver: Consumer<WebSocketMessage>) {
        receivers.add(receiver)
    }
}
