package com.willkamp

import com.willkamp.vial.api.VialServer

fun main() {

    VialServer.create()
            .httpGet("/") { request ->
                request.respondWith { responseBuilder ->
                    responseBuilder.setBodyJson(Pojo("hello GET"))
                }
            }
            .httpPost("/") { request ->
                request.respondWith { responseBuilder ->
                    responseBuilder.setBodyJson(Pojo("hello POST"))
                }
            }
            .httpGet("/v1/foo/:who/fifi") { request ->
                val who = request.pathParam("who")?:("unknown")
                request.respondWith { builder ->
                    builder.setBodyJson(Pojo("hello GET foo - who = $who"))
                }
            }
            .webSocket("/websocket") {webSocket ->
                webSocket.sendText("hello")
                webSocket.receiveText {
                    println("received message = $it")
                }
            }
            .listenAndServeBlocking()
}

private data class Pojo(
        val message: String
)
