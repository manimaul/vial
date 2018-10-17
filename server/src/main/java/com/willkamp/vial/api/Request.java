package com.willkamp.vial.api;

import java.util.Map;
import java.util.Optional;

public interface Request {
  Iterable<Map.Entry<CharSequence, CharSequence>> headers();

  Optional<String> getBodyText();

  Optional<String> pathParam(String key);

  <T> Optional<T> bodyJson(Class<T> clazz);
}
