package com.willkamp.vial.example

import com.willkamp.vial.api.Server
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.codec.http.HttpResponseStatus
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(Main::class.java)

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        log.debug("starting example server")
        Server(port = 8443, useTls = true, h2Capable = true) //todo (WK)
                .get("/", {
                    it.setBody(PojoResponse("hi", "from get"))
                        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .setStatus(HttpResponseStatus.OK)
                })
                .post("/", {
                    it.setBody(PojoResponse("hi", "from post"))
                            .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .setStatus(HttpResponseStatus.OK)
                })
                .start()
    }
}
