package com.willkamp.vial.implementation

import com.willkamp.vial.api.VialConfig
import io.netty.handler.codec.http2.*

internal class Http2HandlerBuilder(
        private val routeRegistry: RouteRegistry,
        private val vialConfig: VialConfig
) : AbstractHttp2ConnectionHandlerBuilder<Http2ConnectionHandler, Http2HandlerBuilder>() {

    public override fun build(): Http2ConnectionHandler {
        return super.build()
    }

    @Throws(Exception::class)
    override fun build(decoder: Http2ConnectionDecoder,
                       encoder: Http2ConnectionEncoder,
                       initialSettings: Http2Settings): Http2ConnectionHandler {
        decoder.frameListener(H2BrokerHandler(encoder, routeRegistry, vialConfig))
        return object : Http2ConnectionHandler(decoder, encoder, initialSettings) {
        }
    }
}
