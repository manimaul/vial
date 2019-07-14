package com.willkamp.vial.implementation;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willkamp.vial.api.Response;
import com.willkamp.vial.api.ResponseBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResponseImpl implements ResponseBuilder, Response {
  private static final AsciiString SERVER_VALUE = AsciiString.of("vial");
  private static final AsciiString JSON = AsciiString.cached("application/json");
  private static final AsciiString TEXT_HTML = AsciiString.cached("text/html");
  private static final AsciiString TEXT_PLAIN = AsciiString.cached("text/plain");
  private Logger log = LoggerFactory.getLogger(ResponseImpl.class);

  private final ByteBufAllocator allocator;
  private HttpResponseStatus status;
  private ByteBuf body;
  private Map<AsciiString, AsciiString> headers;
  private final ObjectMapper objectMapper = Assembly.INSTANCE.getObjectMapper();

  ResponseImpl(ByteBufAllocator allocator) {
    this.allocator = allocator;
  }

  // region IResponseBuilder

  @Override
  public ResponseImpl setStatus(HttpResponseStatus status) {
    this.status = status;
    return this;
  }

  @Override
  public ResponseImpl setBodyJson(Object serializeToJson) {
    ByteBuf byteBuf = allocator.directBuffer();
    try (OutputStream os = new ByteBufOutputStream(byteBuf)) {
      objectMapper.writeValue(os, serializeToJson);
      addHeader(CONTENT_TYPE, JSON);
      body = byteBuf;
    } catch (IOException e) {
      log.error("error serializing json", e);
    }
    return this;
  }

  @Override
  public ResponseImpl setBodyHtml(String html) {
    byte[] bytes = html.getBytes(CharsetUtil.UTF_8);
    body = Unpooled.copiedBuffer(bytes);
    addHeader(CONTENT_TYPE, TEXT_HTML);
    return this;
  }

  @Override
  public ResponseBuilder setBodyData(String contentType, byte[] data) {
    body = Unpooled.copiedBuffer(data);
    addHeader(CONTENT_TYPE, contentType);
    return this;
  }

  @Override
  public ResponseBuilder setBodyText(String text) {
    byte[] bytes = text.getBytes(CharsetUtil.UTF_8);
    body = Unpooled.copiedBuffer(bytes);
    addHeader(CONTENT_TYPE, TEXT_PLAIN);
    return this;
  }

  @Override
  public ResponseImpl addHeader(CharSequence key, CharSequence value) {
    return addHeader(AsciiString.of(key), AsciiString.of(value));
  }

  @Override
  public ResponseImpl addHeader(AsciiString key, AsciiString value) {
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(key, value);
    return this;
  }

  // endregion IResponseBuilder

  ByteBuf getBody() {
    return body == null ? Unpooled.EMPTY_BUFFER : body;
  }

  public HttpResponseStatus getStatus() {
    return status;
  }

  private ByteBuf buildBodyData() {
    return body == null ? Unpooled.EMPTY_BUFFER : body;
  }

  final FullHttpResponse buildFullH1Response() {
    HttpResponseStatus status = this.status;
    if (status == null) {
      status = HttpResponseStatus.OK;
    }

    final DefaultFullHttpResponse response =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buildBodyData());
    response.headers().set(HttpHeaderNames.SERVER, SERVER_VALUE);
    if (headers != null) {
      headers.forEach((key, value) -> response.headers().set(key, value));
    }

    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buildBodyData().readableBytes());
    return response;
  }

  final Http2Headers buildH2Headers() {
    HttpResponseStatus status = this.status;
    if (status == null) {
      status = HttpResponseStatus.OK;
    }

    final DefaultHttp2Headers http2Headers = new DefaultHttp2Headers();
    http2Headers.status(status.codeAsText());
    http2Headers.set(HttpHeaderNames.SERVER, SERVER_VALUE);
    if (headers != null) {
      headers.forEach(http2Headers::set);
    }

    http2Headers.setInt(HttpHeaderNames.CONTENT_LENGTH, buildBodyData().readableBytes());
    return http2Headers;
  }
}
