package com.willkamp.vial.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willkamp.vial.api.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class RequestImpl implements Request {
  private final CharSequence path;
  private Iterable<Map.Entry<CharSequence, CharSequence>> headers;
  private final CompositeByteBuf body;
  private final ObjectMapper objectMapper = Assembly.instance.getObjectMapper();
  private final ByteBufAllocator alloc;

  static RequestImpl fromH2Headers(ByteBufAllocator alloc, Http2Headers headers) {
    return new RequestImpl(alloc, headers.path(), headers);
  }

  static RequestImpl fromStringHeaders(
      ByteBufAllocator alloc,
      CharSequence path,
      Iterable<Map.Entry<String, String>> headers,
      ByteBuf body) {
    List<Map.Entry<CharSequence, CharSequence>> list =
        StreamSupport.stream(headers.spliterator(), false)
            .map(
                it ->
                    new AbstractMap.SimpleEntry<CharSequence, CharSequence>(
                        it.getKey(), it.getValue()))
            .collect(Collectors.toList());
    RequestImpl impl = new RequestImpl(alloc, path, list);
    impl.appendData(body);
    return impl;
  }

  private RequestImpl(
      ByteBufAllocator alloc,
      CharSequence path,
      Iterable<Map.Entry<CharSequence, CharSequence>> headers) {
    this.alloc = alloc;
    this.path = path;
    this.headers = headers;
    this.body = alloc.compositeBuffer();
  }

  int appendData(ByteBuf dataFrame) {
    // CompositeByteBuf releases data
    body.addComponent(true, dataFrame.retain());
    return body.readableBytes();
  }

  @Override
  public Optional<String> getBodyText() {
    return Optional.ofNullable(body).map(it -> it.toString(CharsetUtil.UTF_8));
  }

  @Override
  public <T> Optional<T> bodyJson(Class<T> clazz) {
    return Optional.ofNullable(body)
        .flatMap(
            it -> {
              try {
                T value = objectMapper.readValue(it.array(), clazz);
                return Optional.of(value);
              } catch (IOException e) {
                log.error("error deserializing json", e);
              }
              return Optional.empty();
            });
  }

  @Override
  public Iterable<Map.Entry<CharSequence, CharSequence>> headers() {
    return headers == null ? Collections.emptyList() : headers;
  }
}
