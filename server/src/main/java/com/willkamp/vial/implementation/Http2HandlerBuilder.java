package com.willkamp.vial.implementation;

import com.willkamp.vial.api.RequestHandler;
import io.netty.handler.codec.http2.*;

import java.util.Map;

class Http2HandlerBuilder extends AbstractHttp2ConnectionHandlerBuilder<Http2ConnectionHandler, Http2HandlerBuilder> {

    private final Map<String, RequestHandler> handlers;

    Http2HandlerBuilder(Map<String, RequestHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public Http2ConnectionHandler build() {
        return super.build();
    }

    @Override
    protected Http2ConnectionHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings) throws Exception {
        decoder.frameListener(new H2BrokerHandler(encoder, handlers));
        return new Http2ConnectionHandler(decoder, encoder, initialSettings) {};
    }
}
