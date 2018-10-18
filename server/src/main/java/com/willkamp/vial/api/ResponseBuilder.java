package com.willkamp.vial.api;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;

public interface ResponseBuilder {

  ResponseBuilder setStatus(HttpResponseStatus status);

  ResponseBuilder setBodyJson(Object serializeToJson);

  ResponseBuilder setBodyHtml(String html);

  ResponseBuilder setBodyData(String contentType, byte[] data);

  ResponseBuilder setBodyText(String text);

  ResponseBuilder addHeader(CharSequence key, CharSequence value);

  ResponseBuilder addHeader(AsciiString key, AsciiString value);
}
