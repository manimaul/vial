package com.willkamp.vial.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE
import io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT
import io.netty.handler.codec.http2.DefaultHttp2Headers
import io.netty.handler.codec.http2.Http2Headers
import io.netty.util.AsciiString
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*

val TEXT_HTML = AsciiString.cached("text/html")!!
val TEXT_PLAIN = AsciiString.cached("text/plain")!!
val JSON = AsciiString.cached("application/json")!!


class ResponseBuilder(
        private val allocator: ByteBufAllocator
) {

    companion object {

        private val USER_AGENT_VALUE = AsciiString.of("vial of netty")
        private val MAPPER: ObjectMapper = ObjectMapper()
                .registerModule(ParameterNamesModule())
                .registerModule(Jdk8Module())
                .registerModule(JavaTimeModule())
    }

    private var status: HttpResponseStatus? = null
    private var body: ByteBuf? = null
    private var headers: MutableMap<AsciiString, AsciiString>? = null

    // region API ------------------------------------------------------------------------------------------------------

    fun setStatus(status: HttpResponseStatus): ResponseBuilder {
        this.status = status
        return this
    }

    fun setHtml(body: String) : ResponseBuilder {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        this.body = Unpooled.copiedBuffer(bytes)
        addHeader(CONTENT_TYPE, TEXT_HTML)
        setStatusIfNotSet(HttpResponseStatus.OK)
        return this
    }

    fun setPlainText(body: String): ResponseBuilder {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        this.body = Unpooled.copiedBuffer(bytes)
        addHeader(CONTENT_TYPE, TEXT_PLAIN)
        setStatusIfNotSet(HttpResponseStatus.OK)
        return this
    }

    fun setBodyData(data: ByteBuf): ResponseBuilder {
        body = data
        return this
    }

    @Throws(IOException::class)
    fun setJson(pojo: Any): ResponseBuilder {
        val byteBuf = allocator.directBuffer()
        val stream: OutputStream = ByteBufOutputStream(byteBuf)
        MAPPER.writeValue(stream, pojo)
        body = byteBuf
        addHeader(CONTENT_TYPE, JSON)
        setStatusIfNotSet(HttpResponseStatus.OK)
        return this
    }

    fun addHeader(key: String, value: String): ResponseBuilder {
        return addHeader(AsciiString.of(key), AsciiString.of(value))
    }

    fun addHeader(key: AsciiString, value: AsciiString): ResponseBuilder {
        if (headers == null) {
            headers = HashMap()
        }
        headers!![key] = value
        return this
    }

    // endregion

    // region INTERNAL -------------------------------------------------------------------------------------------------

    private fun setStatusIfNotSet(status: HttpResponseStatus) {
        if (this.status == null) {
            this.status = status
        }
    }

    internal fun buildH1(): FullHttpResponse {
        val status = checkNotNull(status, { "HttpResponseStatus not set" })
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buildBodyData())
        response.headers().set(USER_AGENT, USER_AGENT_VALUE)
        headers?.forEach { key, value -> response.headers().set(key, value) }
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buildBodyData().readableBytes())
        return response
    }

    internal fun buildBodyData(): ByteBuf {
        return body ?: {
            body = Unpooled.EMPTY_BUFFER
            body!!
        }()
    }

    internal fun buildH2Headers(): Http2Headers {
        val status = checkNotNull(status, { "HttpResponseStatus not set" })
        val http2Headers = DefaultHttp2Headers()
        http2Headers.status(status.codeAsText())
        headers?.forEach({ name, value -> http2Headers.set(name, value) })
        http2Headers.setInt(HttpHeaderNames.CONTENT_LENGTH, buildBodyData().readableBytes())
        return http2Headers
    }

    // endregion
}
