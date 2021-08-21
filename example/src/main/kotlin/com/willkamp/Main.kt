package com.willkamp

import com.willkamp.vial.api.EndPointHandler
import com.willkamp.vial.api.Request
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
            .addHandler(FooHandler())
            .webSocket("/websocket") { webSocket ->
                webSocket.sendText("hello")
                webSocket.receiveText {
                    println("received message = $it")
                }
            }
            .listenAndServeBlocking()
}

class FooHandler : EndPointHandler {
    override val route = "/v1/foo/:who/fifi"

    override fun handle(request: Request) {
        val who = request.pathParam("who") ?: ("unknown")
        request.respondWith { builder ->
            builder.setBodyJson(Pojo("hello GET foo - who = $who"))
        }
    }
}


private data class Pojo(
    val message: String
)
