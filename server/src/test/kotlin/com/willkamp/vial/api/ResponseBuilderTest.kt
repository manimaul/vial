package com.willkamp.vial.api

import io.netty.buffer.ByteBufAllocator
import io.netty.util.CharsetUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResponseBuilderTest {

    lateinit var builder: ResponseBuilder

    @BeforeEach
    fun beforeEach() {
        builder = ResponseBuilder(ByteBufAllocator.DEFAULT)
    }

    @Test
    fun html() {
        builder.setHtml("<html></html>")

        assertEquals( "text/html", builder.buildFullH1Response().headers()["content-type"])
        assertEquals("text/html", "${builder.buildH2Headers()["content-type"]}")
        assertEquals("""<html></html>""", builder.buildBodyData().toString(CharsetUtil.UTF_8))
    }

    @Test
    fun plainText() {
        builder.setPlainText("plain text")

        assertEquals( "text/plain", builder.buildFullH1Response().headers()["content-type"])
        assertEquals("text/plain", "${builder.buildH2Headers()["content-type"]}")
        assertEquals("""plain text""", builder.buildBodyData().toString(CharsetUtil.UTF_8))
    }

    @Test
    fun json() {
        builder.setJson(JsonPojo())
        assertEquals( "application/json", builder.buildFullH1Response().headers()["content-type"])
        assertEquals("application/json", "${builder.buildH2Headers()["content-type"]}")
        assertEquals("""{"name":"pojo"}""", builder.buildBodyData().toString(CharsetUtil.UTF_8))
    }

    @Test
    fun h1HeadersHaveServerValue() {
        assertEquals(builder.buildFullH1Response().headers()["server"], "vial")

        builder.addHeader("server", "custom")
        assertEquals(builder.buildFullH1Response().headers()["server"], "custom")
    }

    @Test
    fun h2HeadersHaveServerValue() {
        assertEquals("${builder.buildH2Headers()["server"]}", "vial")

        builder.addHeader("server", "custom")
        assertEquals("${builder.buildH2Headers()["server"]}", "custom")
    }
}

private data class JsonPojo(
        val name: String = "pojo"
)
