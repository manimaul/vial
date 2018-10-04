package com.willkamp.vial.implementation;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

import com.willkamp.vial.api.RequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class H2BrokerHandler extends Http2EventAdapter {

  private final Http2ConnectionEncoder encoder;
  private final IntObjectMap<RequestImpl> requestMap = new IntObjectHashMap<>();
  private final IntObjectMap<RequestHandler> handlerMap = new IntObjectHashMap<>();
  private final int maxPayloadBytes;

  private final Map<String, RequestHandler> handlers;

  H2BrokerHandler(Http2ConnectionEncoder encoder, Map<String, RequestHandler> handlers) {
    this.encoder = encoder;
    this.handlers = handlers;
    this.maxPayloadBytes = Assembly.instance.getVialConfig().getMaxContentLength();
    encoder.connection().addListener(this);
  }

  @Override
  public void onHeadersRead(
      ChannelHandlerContext ctx,
      int streamId,
      Http2Headers headers,
      int padding,
      boolean endOfStream) {

    long contentLength = 0;
    try {
      contentLength = headers.getLong(CONTENT_LENGTH, 0);
    } catch (NumberFormatException nfe) {
      // Malformed header, ignore.
      // This isn't supposed to happen, but does; see https://github.com/netty/netty/issues/7710 .
    }

    if (contentLength > maxPayloadBytes) {
      writeTooLargeResponse(ctx, streamId);
      return;
    }

    @Nullable RequestImpl request = requestMap.get(streamId);
    RequestHandler handler = handlerMap.get(streamId);
    if (request == null) {
      request = RequestImpl.fromH2Headers(ctx.alloc(), headers);
      if (!endOfStream) requestMap.put(streamId, request);
    }

    if (handler == null) {
      String sig = String.format("%s_%s", headers.method(), headers.path());
      handler = handlers.get(sig);
      if (!endOfStream) handlerMap.put(streamId, handler);
    }

    // If there's no data expected, call the handler. Else, pass the handler and request through in
    // the context.
    if (endOfStream) {
      try {
        ResponseImpl response =
            (ResponseImpl) handler.handle(request, new ResponseImpl(ctx.alloc()));
        writeResponse(ctx, streamId, response);
      } catch (Exception e) {
        log.error("route handler error", e);
        writeResponse(
            ctx, streamId, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER);
      }
    }
  }

  @Override
  public void onHeadersRead(
      ChannelHandlerContext ctx,
      int streamId,
      Http2Headers headers,
      int streamDependency,
      short weight,
      boolean exclusive,
      int padding,
      boolean endOfStream) {
    // Ignore stream priority.
    onHeadersRead(ctx, streamId, headers, padding, endOfStream);
  }

  @Override
  public int onDataRead(
      ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
      throws Http2Exception {

    RequestImpl request = requestMap.get(streamId);
    int totalRead = request.appendData(data);
    int processed = data.readableBytes() + padding;
    if (totalRead > maxPayloadBytes) {
      writeTooLargeResponse(ctx, streamId);
      return processed;
    }
    if (endOfStream) {
      RequestHandler handler = handlerMap.get(streamId);
      assert handler != null; //

      try {
        ResponseImpl response =
            (ResponseImpl) handler.handle(request, new ResponseImpl(ctx.alloc()));
        writeResponse(ctx, streamId, response);
        return processed;
      } catch (Exception e) {
        log.error("Error in handling Route", e);
        writeResponse(
            ctx, streamId, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER);
      }
    }
    return super.onDataRead(ctx, streamId, data, padding, endOfStream);
  }

  private void writeTooLargeResponse(ChannelHandlerContext ctx, int streamId) {
    // Close request & channel to prevent overflow.
    writeResponse(
        ctx, streamId, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, Unpooled.EMPTY_BUFFER);
    ctx.flush();
    ctx.close();
  }

  private void writeResponse(
      ChannelHandlerContext ctx, int streamId, HttpResponseStatus status, ByteBuf body) {
    Http2Headers headers = new DefaultHttp2Headers(true);
    headers.setInt(CONTENT_LENGTH, body.readableBytes());
    headers.status(status.codeAsText());
    encoder.writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise());
    encoder.writeData(ctx, streamId, body, 0, true, ctx.newPromise());
  }

  private void writeResponse(ChannelHandlerContext ctx, int streamId, ResponseImpl response) {
    Http2Headers headers = response.buildH2Headers();
    encoder.writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise());
    encoder.writeData(ctx, streamId, response.getBody(), 0, true, ctx.newPromise());
  }
}
