package com.willkamp.vial.internal

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.api.RequestHandler
import io.netty.handler.codec.http2.*

import io.netty.handler.logging.LogLevel.INFO

internal class RestHttp2HandlerBuilder internal constructor(
        private val handlers: ImmutableMap<String, RequestHandler>
) : AbstractHttp2ConnectionHandlerBuilder<H2ConnectionHandler, RestHttp2HandlerBuilder>() {

    companion object {
        private val logger = Http2FrameLogger(INFO)
    }

    init {
        frameLogger(logger)
    }

    public override fun build(): H2ConnectionHandler {
        return super.build()
    }

    override fun build(decoder: Http2ConnectionDecoder,
                       encoder: Http2ConnectionEncoder,
                       initialSettings: Http2Settings): H2ConnectionHandler {
        val handler = H2ConnectionHandler(decoder, encoder, initialSettings, handlers)
        frameListener(handler)
        return handler
    }
}
