package com.willkamp;

import com.willkamp.vial.api.VialServer;

public class Main {
  public static void main(String[] args) {
    VialServer.create()
        .get(
            "/", ((request, responseBuilder) -> responseBuilder.setBodyJson(new Pojo("hello GET"))))
        .post(
            "/",
            ((request, responseBuilder) -> responseBuilder.setBodyJson(new Pojo("hello POST"))))
        .listenAndServeBlocking();
  }

  static class Pojo {
    private final String message;

    Pojo(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
