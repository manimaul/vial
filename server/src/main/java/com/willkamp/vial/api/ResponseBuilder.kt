package com.willkamp.vial.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.AsciiString

interface ResponseBuilder {

    fun setStatus(status: HttpResponseStatus): ResponseBuilder

    fun setBodyJson(serializeToJson: Any): ResponseBuilder

    fun setBodyHtml(html: String): ResponseBuilder

    fun setBodyData(contentType: String, data: ByteArray): ResponseBuilder

    fun setBodyText(text: String): ResponseBuilder

    fun addHeader(key: CharSequence, value: CharSequence): ResponseBuilder

    fun addHeader(key: AsciiString, value: AsciiString): ResponseBuilder
}
