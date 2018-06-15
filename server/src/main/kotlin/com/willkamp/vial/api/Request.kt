package com.willkamp.vial.api

import com.willkamp.vial.internal.MAPPER
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpMethod
import io.netty.util.CharsetUtil


data class Request(
        val path: CharSequence,
        private val methodStr: CharSequence,
        val headers: Iterable<Map.Entry<CharSequence, CharSequence>>,
        val body: ByteBuf?
) {
    val method: HttpMethod
        get() = HttpMethod.valueOf("$methodStr")

    val bodyText: String?
        get() {
            return body?.toString(CharsetUtil.UTF_8)
        }

    inline fun <reified T> bodyJson(): T? {
        return bodyText?.let {
            return MAPPER.readValue(it, T::class.java)
        }
    }
}