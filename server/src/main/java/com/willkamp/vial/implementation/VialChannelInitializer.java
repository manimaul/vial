package com.willkamp.vial.implementation;

import com.willkamp.vial.api.RequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

class VialChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final SslContext sslContext;
  private Map<String, RequestHandler> handlers = Collections.emptyMap();
  private final VialConfig vialConfig;

  VialChannelInitializer(@Nullable SslContext sslContext) {
    this.sslContext = sslContext;
    this.vialConfig = Assembly.instance.getVialConfig();
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    if (sslContext == null) {
      configureH1(ch.pipeline());
    } else {
      configureH2(ch);
    }
  }

  void setHandlers(Map<String, RequestHandler> handlers) {
    this.handlers = Map.copyOf(handlers);
  }

  private void configureH2(SocketChannel ch) {
    assert sslContext != null;
    ch.pipeline()
        .addLast(sslContext.newHandler(ch.alloc()), new AlpnHandler(handlers, this::configureH1));
  }

  private void configureH1(ChannelPipeline pipeline) {
    pipeline
        .addLast("server codec duplex", new HttpServerCodec())
        .addLast(
            "message size limit aggregator",
            new HttpObjectAggregator(vialConfig.getMaxContentLength()))
        .addLast("request handler", new H1BrokerHandler(handlers));
  }
}