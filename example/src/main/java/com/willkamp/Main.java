package com.willkamp;

import com.willkamp.vial.api.VialServer;

class Main {
    public static void main(String[] args) {
        VialServer.create()
                .httpGet("/", ((request, responseBuilder) ->
                        responseBuilder.setBodyJson(new Pojo("hello GET"))))
                .httpPost("/", ((request, responseBuilder) ->
                        responseBuilder.setBodyJson(new Pojo("hello POST"))))
                .httpGet("/v1/foo/:who/fifi", ((request, responseBuilder) -> {
                    String who = request.pathParamOption("who").orElse("unknown");
                    return responseBuilder.setBodyJson(
                            new Pojo(String.format("hello GET foo - who = %s", who)));
                }))
                .webSocket("/websocket", (webSocket) -> {
                    webSocket.sendText("hello");
                    webSocket.receiveText( msg -> System.out.printf("received message = %s%n", msg));
                })
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