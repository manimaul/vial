package com.willkamp.vial.implementation;

import com.willkamp.vial.api.Request;
import com.willkamp.vial.api.RequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class H1BrokerHandler extends ChannelInboundHandlerAdapter {

  private final Map<String, RequestHandler> handlers;

  H1BrokerHandler(Map<String, RequestHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest message = (FullHttpRequest) msg;
      String pathKey = String.format("%s_%s", message.method(), message.uri());
      log.debug("channel read path key {}", pathKey);
      RequestHandler handler = handlers.get(pathKey);
      final FullHttpResponse response;
      if (handler != null) {
        Request request =
            RequestImpl.fromStringHeaders(
                ctx.alloc(), message.uri(), message.headers(), message.content());
        response =
            ((ResponseImpl) handler.handle(request, new ResponseImpl(ctx.alloc())))
                .buildFullH1Response();
      } else {
        response =
            new ResponseImpl(ctx.alloc())
                .setStatus(HttpResponseStatus.NOT_FOUND)
                .buildFullH1Response();
      }

      ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    } else {
      log.warn("unknown message type {}", msg);
    }
    ctx.fireChannelRead(msg);
  }
}
