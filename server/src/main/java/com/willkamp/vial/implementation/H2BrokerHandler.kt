package com.willkamp.vial.implementation

import com.willkamp.vial.api.VialConfig
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http2.*
import io.netty.util.collection.IntObjectHashMap

internal class H2BrokerHandler(
        private val encoder: Http2ConnectionEncoder,
        private val routeRegistry: RouteRegistry,
        vialConfig: VialConfig
) : Http2EventAdapter() {
    private val requestMap = IntObjectHashMap<RequestImpl>()
    private val handlerMap = IntObjectHashMap<RouteRegistry.Meta>()
    private val maxPayloadBytes: Int = vialConfig.maxContentLength
    private val log = logger()

    init {
        encoder.connection().addListener(this)
    }

    override fun onHeadersRead(
            ctx: ChannelHandlerContext,
            streamId: Int,
            headers: Http2Headers,
            padding: Int,
            endOfStream: Boolean) {

        var contentLength: Long = 0
        try {
            contentLength = headers.getLong(CONTENT_LENGTH, 0)
        } catch (nfe: NumberFormatException) {
            // Malformed header, ignore.
            // This isn't supposed to happen, but does; see https://github.com/netty/netty/issues/7710 .
        }

        if (contentLength > maxPayloadBytes) {
            writeTooLargeResponse(ctx, streamId)
            return
        }

        var request: RequestImpl? = requestMap.get(streamId)
        var meta: RouteRegistry.Meta? = handlerMap.get(streamId)
        if (request == null) {
            request = RequestImpl.fromH2Headers(ctx.alloc(), headers)
            if (!endOfStream) requestMap[streamId] = request
        }

        if (meta == null) {
            meta = routeRegistry.findHandler(headers.method(), headers.path()).orElse(null)
            if (!endOfStream && meta != null) handlerMap[streamId] = meta
        }

        // If there's no data expected, call the handler. Else, pass the handler and request through in
        // the context.
        if (endOfStream) {
            if (meta == null) {
                log.debug(
                        "route handler note found for method: {} and path: {}",
                        headers.method(),
                        headers.path())
                writeResponse(ctx, streamId, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER)
            } else {
                try {
                    val m = meta
                    request.let {
                        request.setPathParamGroupSupplier {
                            m.route?.groups(headers.path()) ?: emptyMap()
                        }
                        val response = meta.handler(request, ResponseImpl(ctx.alloc())) as ResponseImpl
                        writeResponse(ctx, streamId, response)
                    }


                } catch (e: Exception) {
                    log.error("route handler error", e)
                    writeResponse(ctx, streamId, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER)
                }
            }
        }
    }

    override fun onHeadersRead(
            ctx: ChannelHandlerContext,
            streamId: Int,
            headers: Http2Headers,
            streamDependency: Int,
            weight: Short,
            exclusive: Boolean,
            padding: Int,
            endOfStream: Boolean) {
        // Ignore stream priority.
        onHeadersRead(ctx, streamId, headers, padding, endOfStream)
    }

    @Throws(Http2Exception::class)
    override fun onDataRead(ctx: ChannelHandlerContext,
                            streamId: Int,
                            data: ByteBuf,
                            padding: Int,
                            endOfStream: Boolean): Int {

        val request = requestMap.get(streamId)
        val totalRead = request.appendData(data)
        val processed = data.readableBytes() + padding
        if (totalRead > maxPayloadBytes) {
            writeTooLargeResponse(ctx, streamId)
            return processed
        }
        if (endOfStream) {
            val meta = handlerMap.get(streamId)

            try {
                request.setPathParamGroupSupplier {
                    meta.route?.groups(request.path) ?: emptyMap()
                }
                val response = meta.handler(request, ResponseImpl(ctx.alloc())) as ResponseImpl
                writeResponse(ctx, streamId, response)
                return processed
            } catch (e: Exception) {
                log.error("Error in handling Route", e)
                writeResponse(ctx, streamId, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER)
            }

        }
        return super.onDataRead(ctx, streamId, data, padding, endOfStream)
    }

    private fun writeTooLargeResponse(ctx: ChannelHandlerContext?, streamId: Int) {
        // Close request & channel to prevent overflow.
        ctx?.let {
            writeResponse(
                    it, streamId, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, Unpooled.EMPTY_BUFFER)
            it.flush()
            it.close()
        }
    }

    private fun writeResponse(ctx: ChannelHandlerContext,
                              streamId: Int,
                              status: HttpResponseStatus,
                              body: ByteBuf) {
        val headers = DefaultHttp2Headers(true)
        headers.setInt(CONTENT_LENGTH, body.readableBytes())
        headers.status(status.codeAsText())
        encoder.writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise())
        encoder.writeData(ctx, streamId, body, 0, true, ctx.newPromise())
    }

    private fun writeResponse(ctx: ChannelHandlerContext, streamId: Int, response: ResponseImpl) {
        val headers = response.buildH2Headers()
        encoder.writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise())
        encoder.writeData(ctx, streamId, response.getBody(), 0, true, ctx.newPromise())
    }
}
