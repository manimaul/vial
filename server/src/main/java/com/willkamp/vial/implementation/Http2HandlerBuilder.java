package com.willkamp.vial.implementation;

import io.netty.handler.codec.http2.*;

class Http2HandlerBuilder
    extends AbstractHttp2ConnectionHandlerBuilder<Http2ConnectionHandler, Http2HandlerBuilder> {

  @Override
  public Http2ConnectionHandler build() {
    return super.build();
  }

  @Override
  protected Http2ConnectionHandler build(
      Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings)
      throws Exception {
    decoder.frameListener(new H2BrokerHandler(encoder));
    return new Http2ConnectionHandler(decoder, encoder, initialSettings) {};
  }
}
