package com.willkamp.vial.api

import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import okhttp3.Request
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.Closeable


class ServerFunctionalTest {

    private var okHttpClient: OkHttpClient? = null
    private var server: Closeable? = null

    private fun setupClient(protocol: String) {
        okHttpClient = UnsafeClient.getUnsafeClient("h2" == protocol)
    }

    private fun setupServer(protocol: String) {
        val mp = if ("h2" == protocol) Protocol.HTTP_2 else Protocol.HTTP_1_1

        server = VialServer(
                port = 8443,
                minimumProtocol = mp,
                tlsContext = TlsContext.forServerSelfSigned()
        ).get("/v1/test", { _, response ->
            response.setPlainText("expected GET response")
        }).post("/v1/test", { _, response ->
            response.setPlainText("expected POST response")
        }).buildAndServeAsync().get()

    }

    @AfterEach
    fun afterEach() {
        server?.close()
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