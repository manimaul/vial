package com.willkamp.vial.implementation;

import com.willkamp.vial.api.RequestHandler;
import io.netty.handler.codec.http.HttpMethod;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

class RouteRegistry {
  private final Map<HttpMethod, List<Meta>> routeHandlers = new HashMap<>();

  void registerRoute(HttpMethod method, String routePattern, RequestHandler handler) {
    List<Meta> list = routeHandlers.computeIfAbsent(method, k -> new ArrayList<>());
    list.add(new Meta(Route.build(routePattern), handler));
  }

  Optional<Meta> findHandler(CharSequence method, CharSequence path) {
    return findHandler(HttpMethod.valueOf(method.toString()), path);
  }

  Optional<Meta> findHandler(HttpMethod method, CharSequence path) {
    return Optional.ofNullable(routeHandlers.get(method))
        .flatMap(
            metaList -> metaList.stream().filter(meta -> meta.route.matches(path)).findFirst());
  }

  @Getter
  @AllArgsConstructor
  static class Meta {
    private final Route route;
    private final RequestHandler handler;
  }
}
