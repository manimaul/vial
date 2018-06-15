package com.willkamp.vial.internal

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.api.Request
import com.willkamp.vial.api.RequestHandler
import com.willkamp.vial.api.ResponseBuilder
import io.netty.buffer.ByteBuf
import io.netty.buffer.CompositeByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http2.*
import io.netty.util.AttributeKey
import kotlin.collections.HashMap

internal class H2ConnectionHandler internal constructor(
        decoder: Http2ConnectionDecoder,
        encoder: Http2ConnectionEncoder,
        initialSettings: Http2Settings,
        private val handlers: ImmutableMap<String, RequestHandler>
) : Http2ConnectionHandler(decoder, encoder, initialSettings), Http2FrameListener {

    companion object {

        private val HEADER_KEY = AttributeKey.newInstance<MutableMap<Int, Http2Headers>>("header_key")
        private val COMP_KEY = AttributeKey.newInstance<MutableMap<Int, CompositeByteBuf>>("comp_key")

        private fun headersMap(ctx: ChannelHandlerContext): MutableMap<Int, Http2Headers> {
            return ctx.channel().attr(HEADER_KEY).get() ?: {
                val map = HashMap<Int, Http2Headers>()
                ctx.channel().attr(HEADER_KEY).set(map)
                map
            }()
        }

        private fun compositeByteBuf(ctx: ChannelHandlerContext, streamId: Int): CompositeByteBuf {
            val map = ctx.channel().attr(COMP_KEY).get() ?: {
                val map = HashMap<Int, CompositeByteBuf>()
                ctx.channel().attr(COMP_KEY).set(map)
                map
            }()
            return map[streamId] ?: {
                val buf = ctx.alloc().compositeBuffer()
                map[streamId] = buf
                buf
            }()
        }

        fun setHeaders(ctx: ChannelHandlerContext,
                       streamId: Int,
                       headers: Http2Headers) {
            val saveHeaders = getHeaders(ctx, streamId)?.add(headers) ?: headers
            headersMap(ctx)[streamId] = saveHeaders
        }

        fun getHeaders(ctx: ChannelHandlerContext,
                       streamId: Int?): Http2Headers? {
            return headersMap(ctx)[streamId]
        }

        private fun http1HeadersToHttp2Headers(request: FullHttpRequest): Http2Headers {
            val host = request.headers().get(HttpHeaderNames.HOST)
            val http2Headers = DefaultHttp2Headers()
                    .method(HttpMethod.GET.asciiName())
                    .path(request.uri())
                    .scheme(HttpScheme.HTTP.name())
            if (host != null) {
                http2Headers.authority(host)
            }
            return http2Headers
        }
    }

    /**
     * Handles the cleartext HTTP upgrade event. If an upgrade occurred, sends a simple response via HTTP/2
     * on stream 1 (the stream specifically reserved for cleartext HTTP upgrade).
     */
    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext,
                                    evt: Any) {
        if (evt is HttpServerUpgradeHandler.UpgradeEvent) {
            onHeadersRead(ctx, 1, http1HeadersToHttp2Headers(evt.upgradeRequest()), 0, true)
        }
        super.userEventTriggered(ctx, evt)
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext,
                                 cause: Throwable) {
        super.exceptionCaught(ctx, cause)
        ctx.close()
    }

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        super.channelReadComplete(ctx)
    }

    private fun errorResponseBuilder(ctx: ChannelHandlerContext): ResponseBuilder {
        return ResponseBuilder(ctx.alloc())
                .setBodyData(Unpooled.EMPTY_BUFFER)
                .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
    }

    private fun handler(headers: Http2Headers): RequestHandler? {
        return handlers["${headers.method()}_${headers.path()}"]
    }

    private fun sendResponse(ctx: ChannelHandlerContext,
                             streamId: Int,
                             requestBody: ByteBuf?) {
        val responseBuilder = getHeaders(ctx, streamId)?.let { headers ->
            val method = HttpMethod.valueOf("${headers.method()}")
            headers.method()
            val request = Request(headers.path(), headers.method(), headers, requestBody)
            handler(headers)?.let { handler ->
                handler(request, ResponseBuilder(ctx.alloc()))
            }
        } ?: errorResponseBuilder(ctx)

        // Send a frame for the response status
        val headers = responseBuilder.buildH2Headers()
        encoder().writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise())
        encoder().writeData(ctx, streamId, responseBuilder.buildBodyData(), 0, true, ctx.newPromise())

        // no need to call flush as channelReadComplete(...) will take care of it.
    }

    override fun onDataRead(ctx: ChannelHandlerContext,
                            streamId: Int,
                            data: ByteBuf,
                            padding: Int,
                            endOfStream: Boolean): Int {
        val processed = data.readableBytes() + padding
        data.retain(1)
        val buf = compositeByteBuf(ctx, streamId)
        buf.addComponent(data)
        if (endOfStream) {
            sendResponse(ctx, streamId, buf)
        }
        return processed
    }

    override fun onHeadersRead(ctx: ChannelHandlerContext,
                               streamId: Int,
                               headers: Http2Headers,
                               padding: Int,
                               endOfStream: Boolean) {
        setHeaders(ctx, streamId, headers)
        if (endOfStream) {
            sendResponse(ctx, streamId, compositeByteBuf(ctx, streamId))
        }
    }

    override fun onHeadersRead(ctx: ChannelHandlerContext,
                               streamId: Int,
                               headers: Http2Headers,
                               streamDependency: Int,
                               weight: Short,
                               exclusive: Boolean,
                               padding: Int,
                               endOfStream: Boolean) {
        onHeadersRead(ctx, streamId, headers, padding, endOfStream)
    }

    override fun onPriorityRead(ctx: ChannelHandlerContext,
                                streamId: Int,
                                streamDependency: Int,
                                weight: Short,
                                exclusive: Boolean) {
    }

    override fun onRstStreamRead(ctx: ChannelHandlerContext,
                                 streamId: Int,
                                 errorCode: Long) {
    }

    override fun onSettingsAckRead(ctx: ChannelHandlerContext) {}

    override fun onSettingsRead(ctx: ChannelHandlerContext,
                                settings: Http2Settings) {
    }

    override fun onPingRead(ctx: ChannelHandlerContext,
                            data: ByteBuf) {

    }

    override fun onPingAckRead(ctx: ChannelHandlerContext,
                               data: ByteBuf) {

    }

    override fun onPushPromiseRead(ctx: ChannelHandlerContext,
                                   streamId: Int,
                                   promisedStreamId: Int,
                                   headers: Http2Headers,
                                   padding: Int) {
    }

    override fun onGoAwayRead(ctx: ChannelHandlerContext,
                              lastStreamId: Int,
                              errorCode: Long,
                              debugData: ByteBuf) {
    }

    override fun onWindowUpdateRead(ctx: ChannelHandlerContext,
                                    streamId: Int,
                                    windowSizeIncrement: Int) {
    }

    override fun onUnknownFrame(ctx: ChannelHandlerContext,
                                frameType: Byte,
                                streamId: Int,
                                flags: Http2Flags,
                                payload: ByteBuf) {
    }
}

