package com.willkamp.vial.example

import com.willkamp.vial.api.VialServer
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(Main::class.java)

data class Response(val message: String = "OK")

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        log.debug("starting example server")

        VialServer()
                .get("/", { request, response ->
                    log.debug("get request: $request")
                    response.setJson(pojo = Response(message = "hello from get"))
                })
                .post("/", { request, response ->
                    log.debug("post request: $request")
                    response.setJson(pojo = Response(message= "hello from post"))
                })
                .buildAndServe()
    }
}
