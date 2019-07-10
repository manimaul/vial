package com.willkamp

import com.willkamp.vial.api.VialServer

fun main() {
    VialServer.create()
            .get("/") { _, responseBuilder ->
                responseBuilder.setBodyJson(Pojo("hello GET"))
            }
            .post("/") { _, responseBuilder ->
                responseBuilder.setBodyJson(Pojo("hello POST"))
            }
            .get("/v1/foo/:who/fifi") { request, responseBuilder ->
                val who = request.pathParam("who").orElse("unknown")
                responseBuilder.setBodyJson(Pojo("hello GET foo - who = $who"))
            }
            .listenAndServeBlocking()
}

private data class Pojo(
        val message: String
)
