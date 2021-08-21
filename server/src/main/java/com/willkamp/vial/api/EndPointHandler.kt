package com.willkamp.vial.api

import io.netty.handler.codec.http.HttpMethod

interface EndPointHandler {
    val method: HttpMethod
        get() = HttpMethod.GET

    val route: String
    fun handle(request: Request)
}
