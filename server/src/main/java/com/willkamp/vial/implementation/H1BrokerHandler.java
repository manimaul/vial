package com.willkamp.vial.implementation;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class H1BrokerHandler extends ChannelInboundHandlerAdapter {

  private final RouteRegistry routeRegistry = Assembly.instance.getRouteRegistry();

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest message = (FullHttpRequest) msg;
      RouteRegistry.Meta handler =
          routeRegistry.findHandler(message.method(), message.uri()).orElse(null);
      final FullHttpResponse response;
      if (handler != null) {
        RequestImpl request =
            RequestImpl.fromStringHeaders(
                ctx.alloc(), message.uri(), message.headers(), message.content());
        request.setPathParamGroupSupplier(() -> handler.getRoute().groups(message.uri()));
        response =
            ((ResponseImpl) handler.getHandler().handle(request, new ResponseImpl(ctx.alloc())))
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
