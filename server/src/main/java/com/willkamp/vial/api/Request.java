package com.willkamp.vial.api;

import java.util.Map;
import java.util.Optional;

public interface Request {
  Iterable<Map.Entry<CharSequence, CharSequence>> headers();

  Optional<String> getBodyText();

  <T> Optional<T> bodyJson(Class<T> clazz);
}
