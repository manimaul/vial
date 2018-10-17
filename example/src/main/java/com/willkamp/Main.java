package com.willkamp;

import com.willkamp.vial.api.VialServer;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Main {
  public static void main(String[] args) {
    VialServer.create()
        .get(
            "/", ((request, responseBuilder) -> responseBuilder.setBodyJson(new Pojo("hello GET"))))
        .post(
            "/",
            ((request, responseBuilder) -> responseBuilder.setBodyJson(new Pojo("hello POST"))))
        .get(
            "/v1/foo/:who/fifi",
            ((request, responseBuilder) -> {
              String who = request.pathParam("who").orElse("unknown");
              return responseBuilder.setBodyJson(
                  new Pojo(String.format("hello GET foo - who = %s", who)));
            }))
        .listenAndServeBlocking();
  }

  @AllArgsConstructor
  @Getter
  private static class Pojo {
    private final String message;
  }
}
