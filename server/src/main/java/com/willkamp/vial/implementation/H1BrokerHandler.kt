package com.willkamp.vial.implementation

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

internal class H1BrokerHandler(
        private val routeRegistry: RouteRegistry
) : ChannelInboundHandlerAdapter() {

    private val log = logger()

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is FullHttpRequest) {
            val response = routeRegistry.findHandler(msg.method(), msg.uri()).orElse(null)?.let {
                val request = RequestImpl.fromStringHeaders(
                        ctx.alloc(), msg.uri(), msg.headers(), msg.content())
                request.setPathParamGroupSupplier {
                    it.route?.groups(msg.uri()) ?: emptyMap()
                }
                val impl = it.handler.invoke(request, ResponseImpl(ctx.alloc())) as ResponseImpl
                impl.buildFullH1Response()
            } ?: ResponseImpl(ctx.alloc())
                    .setStatus(HttpResponseStatus.NOT_FOUND)
                    .buildFullH1Response()
            ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
        } else {
            log.warn("unknown message type {}", msg)
        }
        ctx.fireChannelRead(msg)
    }
}
