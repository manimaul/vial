package com.willkamp.vial.implementation;

import com.willkamp.vial.api.Request;
import com.willkamp.vial.api.ResponseBuilder;
import io.netty.handler.codec.http.HttpMethod;
import java.util.*;

import kotlin.jvm.functions.Function2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

class RouteRegistry {
  private final Map<HttpMethod, List<Meta>> routeHandlers = new HashMap<>();

  void registerRoute(HttpMethod method, String routePattern, @NotNull Function2<? super Request, ? super ResponseBuilder, ? extends ResponseBuilder> handler) {
    List<Meta> list = routeHandlers.computeIfAbsent(method, k -> new ArrayList<>());
    list.add(new Meta(Route.Companion.build(routePattern), handler));
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
    final Route route;
    final @NotNull Function2<? super Request, ? super ResponseBuilder, ? extends ResponseBuilder> handler;
  }
}
