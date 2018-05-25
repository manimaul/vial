package com.willkamp.vial.internal

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.api.RequestHandler
import com.willkamp.vial.api.ResponseBuilder
import io.netty.buffer.Unpooled
import io.netty.buffer.Unpooled.copiedBuffer
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(ChannelRequestInboundHandler::class.java)

internal class ChannelRequestInboundHandler internal constructor(
        private val handlers: ImmutableMap<String, RequestHandler>
) : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        log.debug("read $msg")
        if (msg is FullHttpRequest) {
            val pathKey = "${msg.method().name()}_${msg.uri()}"
            val response = handlers[pathKey]?.let { handler ->
                handler(ResponseBuilder(ctx.alloc()))
                        .buildH1()
            } ?: ResponseBuilder(ctx.alloc())
                    .setStatus(HttpResponseStatus.NOT_FOUND)
                    .buildH1()
            log.debug("write flushing response $response")
            ctx.writeAndFlush(response)
            return
        }
        log.debug("channel read msg:$msg")
        ctx.fireChannelRead(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("error: $cause")
        val data = cause.message?.let { copiedBuffer(it.toByteArray()) } ?: Unpooled.EMPTY_BUFFER
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, data)
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, data.readableBytes())
        ctx.writeAndFlush(response)
    }
}
