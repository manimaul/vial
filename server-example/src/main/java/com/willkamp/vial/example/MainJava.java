package com.willkamp.vial.example;

import com.willkamp.vial.api.VialServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainJava {

    public static void main(String[] args) {
        log.debug("starting example server");

        new VialServer()
                .get("/", (request, response) -> {
                    log.debug("get request {}", request);
                    return response.setJson(new Response("hello from get"));
                })
                .post("/", (request, response) -> {
                    log.debug("post request {}", request);
                    return response.setJson(new Response("hello from post"));
                })
                .buildAndServe();
    }

    @Value
    @AllArgsConstructor
    private static class Response {
        private final  String message;
    }
}
