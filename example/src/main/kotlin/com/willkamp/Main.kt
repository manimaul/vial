package com.willkamp

import com.willkamp.vial.api.ResponseBuilder
import com.willkamp.vial.api.VialServer

fun main() {

    VialServer.create()
            .httpGet("/") { _, responseBuilder: ResponseBuilder ->
                responseBuilder.setBodyJson(Pojo("hello GET"))
            }
            .httpPost("/") { _, responseBuilder ->
                responseBuilder.setBodyJson(Pojo("hello POST"))
            }
            .httpGet("/v1/foo/:who/fifi") { request, responseBuilder ->
                val who = request.pathParam("who")?:("unknown")
                responseBuilder.setBodyJson(Pojo("hello GET foo - who = $who"))
            }
            .listenAndServeBlocking()
}

private data class Pojo(
        val message: String
)
