package com.willkamp.vial.implementation

import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE

import com.willkamp.vial.api.Response
import com.willkamp.vial.api.ResponseBuilder
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http2.DefaultHttp2Headers
import io.netty.handler.codec.http2.Http2Headers
import io.netty.util.AsciiString
import io.netty.util.CharsetUtil
import java.io.IOException
import java.io.OutputStream
import java.util.HashMap

internal class ResponseImpl(private val allocator: ByteBufAllocator) : ResponseBuilder, Response {
    private val log = logger<ResponseImpl>()
    private var status: HttpResponseStatus? = null
    private var body: ByteBuf? = null
    private var headers: MutableMap<AsciiString, AsciiString>? = null
    private val objectMapper = Assembly.objectMapper

    // region IResponseBuilder

    override fun setStatus(status: HttpResponseStatus): ResponseImpl {
        this.status = status
        return this
    }

    override fun setBodyJson(serializeToJson: Any): ResponseImpl {
        val byteBuf = allocator.directBuffer()
        try {
            ByteBufOutputStream(byteBuf).use { os: OutputStream ->
                objectMapper.writeValue(os, serializeToJson)
                addHeader(CONTENT_TYPE, JSON)
                body = byteBuf
            }
        } catch (e: IOException) {
            log.error("error serializing json", e)
        }

        return this
    }

    override fun setBodyHtml(html: String): ResponseImpl {
        val bytes = html.toByteArray(CharsetUtil.UTF_8)
        body = Unpooled.copiedBuffer(bytes)
        addHeader(CONTENT_TYPE, TEXT_HTML)
        return this
    }

    override fun setBodyData(contentType: String, data: ByteArray): ResponseBuilder {
        body = Unpooled.copiedBuffer(data)
        addHeader(CONTENT_TYPE, contentType)
        return this
    }

    override fun setBodyText(text: String): ResponseBuilder {
        val bytes = text.toByteArray(CharsetUtil.UTF_8)
        body = Unpooled.copiedBuffer(bytes)
        addHeader(CONTENT_TYPE, TEXT_PLAIN)
        return this
    }

    override fun addHeader(key: CharSequence, value: CharSequence): ResponseImpl {
        return addHeader(AsciiString.of(key), AsciiString.of(value))
    }

    override fun addHeader(key: AsciiString, value: AsciiString): ResponseImpl {
        if (headers == null) {
            headers = HashMap()
        }
        headers!![key] = value
        return this
    }

    // endregion IResponseBuilder

    fun getBody(): ByteBuf {
        return body ?: Unpooled.EMPTY_BUFFER
    }

    fun getStatus(): HttpResponseStatus? {
        return status
    }

    private fun buildBodyData(): ByteBuf {
        return body ?: Unpooled.EMPTY_BUFFER
    }

    fun buildFullH1Response(): FullHttpResponse {
        var status = this.status
        if (status == null) {
            status = HttpResponseStatus.OK
        }

        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status!!, buildBodyData())
        response.headers().set(HttpHeaderNames.SERVER, SERVER_VALUE)
        if (headers != null) {
            headers!!.forEach { (key, value) -> response.headers().set(key, value) }
        }

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buildBodyData().readableBytes())
        return response
    }

    fun buildH2Headers(): Http2Headers {
        var status = this.status
        if (status == null) {
            status = HttpResponseStatus.OK
        }

        val http2Headers = DefaultHttp2Headers()
        http2Headers.status(status!!.codeAsText())
        http2Headers.set(HttpHeaderNames.SERVER, SERVER_VALUE)
        headers?.forEach { name, value -> http2Headers.set(name, value) }
        http2Headers.setInt(HttpHeaderNames.CONTENT_LENGTH, buildBodyData().readableBytes())
        return http2Headers
    }

    companion object {
        private val SERVER_VALUE = AsciiString.of("vial")
        private val JSON = AsciiString.cached("application/json")
        private val TEXT_HTML = AsciiString.cached("text/html")
        private val TEXT_PLAIN = AsciiString.cached("text/plain")
    }
}
