package com.willkamp.vial.api

import io.netty.handler.codec.http.HttpMethod
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import java.io.Closeable


private val log = LoggerFactory.getLogger(ServerFunctionalTest::class.java)

class ServerFunctionalTest {

    private var okHttpClient: OkHttpClient? = null
    private var serverCloser: Closeable? = null

    private fun setupClient(protocol: String) {
        okHttpClient = UnsafeClient.getUnsafeClient("h2" == protocol)
    }

    private fun setupServer(protocol: String) {
        val mp = if ("h2" == protocol) Protocol.HTTP_2 else Protocol.HTTP_1_1
        val vialServer = VialServer(
                port = 8443,
                minimumProtocol = mp,
                tlsContext = TlsContext.forServerSelfSigned()
        )
        arrayOf(
                HttpMethod.OPTIONS,
                HttpMethod.GET,
                HttpMethod.HEAD,
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.PATCH,
                HttpMethod.DELETE,
                HttpMethod.TRACE,
                HttpMethod.CONNECT
        ).forEach {
            vialServer.request(it, "/v1/test", { request, response ->
                log.warn("request: $request")
                response.setPlainText("expected ${request.method} response")
            })
        }
        serverCloser = vialServer.buildAndServeAsync().get()
    }

    @AfterEach
    fun afterEach() {
        serverCloser?.close()
    }

    @ParameterizedTest
    @ValueSource(strings = ["h1"/* todo: (WK) , "h2"*/])
    fun testServer(protocol: String) {
        setupClient(protocol)
        setupServer(protocol)
        val response = okHttpClient?.newCall(Request.Builder()
                .get()
                .url("https://127.0.0.1:8443/v1/test")
                .build())
                ?.execute()

        assertEquals("expected GET response", response?.body()?.string())
    }
}