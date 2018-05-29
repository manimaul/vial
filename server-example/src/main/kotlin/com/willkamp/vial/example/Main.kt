package com.willkamp.vial.example

import com.willkamp.vial.api.Server
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(Main::class.java)

data class Response(val message: String = "OK")

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        log.debug("starting example server")

        Server(port = 8080)
                .get("/", { request, response ->
                    log.debug("get request: $request")
                    response.setHtml("""<html>hi<html""")
                })
                .post("/", { request, response ->
                    log.debug("post request: $request")
                    response.setJson(pojo = Response())
                }).serve()
    }
}
