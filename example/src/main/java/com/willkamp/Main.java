package com.willkamp;

import com.willkamp.vial.api.VialServer;
import com.willkamp.vial.api.WebSocketBinMessage;
import com.willkamp.vial.api.WebSocketClosed;
import com.willkamp.vial.api.WebSocketTextMessage;

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
                .webSocket("/websocket", (sender) -> {
                    sender.send(new WebSocketTextMessage("hello"));
                    return null;
                }, (receivedMsg) -> {
                    if (receivedMsg instanceof WebSocketTextMessage) {
                        WebSocketTextMessage textMessage = (WebSocketTextMessage) receivedMsg;
                        System.out.printf("received message = %s%n", textMessage.getText());
                    } else if (receivedMsg instanceof WebSocketBinMessage) {
                        WebSocketBinMessage binMessage = (WebSocketBinMessage) receivedMsg;
                        System.out.printf("received data message size = %d%n", binMessage.getBin().length);
                    } else if (receivedMsg instanceof WebSocketClosed) {
                        System.out.println("received websocket closed");
                    }
                    return null;
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