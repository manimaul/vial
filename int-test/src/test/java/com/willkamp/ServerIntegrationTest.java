package com.willkamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.willkamp.vial.api.VialServer;
import com.willkamp.vial.api.WebSocketTextMessage;
import kotlin.collections.SetsKt;
import lombok.*;
import okhttp3.*;
import okio.ByteString;
import org.apache.groovy.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ServerIntegrationTest {

    private Closeable server;
    private TestServer testServer;

    @BeforeEach
    void beforeEach() throws Exception {
        testServer = new TestServer();
        server = testServer.start();
    }

    @AfterEach
    void afterEach() throws Exception {
        server.close();
    }

    @Test
    void testWebSocket() throws Exception {
        OkHttpClient client = OkHttpUnsafe.getUnsafeClient(Protocol.HTTP_1_1, Protocol.HTTP_2);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> clientRecievedMessages = new ArrayList<>();
        client.newWebSocket(new Request.Builder().url("https://127.0.0.1:8443/websocket").get().build(), new WebSocketListener() {
            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                clientRecievedMessages.add(text);
                webSocket.close(1000, "done");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                fail("not expecting binary message");
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                assertEquals(101, response.code());
                webSocket.send("hello");
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5000, TimeUnit.SECONDS));

        assertEquals(1, testServer.receivedWsMessages.size());
        assertEquals("hello", testServer.receivedWsMessages.get(0));

        assertEquals(1, clientRecievedMessages.size());
        assertEquals("hello ws", clientRecievedMessages.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"h2,http/1.1", "http/1.1"})
    void testSlashGet(String protocol) throws Exception {
        OkHttpClient client = OkHttpUnsafe.getUnsafeClient(protocol.split(","));
        Response response =
                client
                        .newCall(new Request.Builder().url("https://127.0.0.1:8443/").get().build())
                        .execute();

        assertEquals(200, response.code());
        if (protocol.equals("h2,http/1.1")) {
            assertEquals(Protocol.HTTP_2, response.protocol());
        } else {
            assertEquals(Protocol.HTTP_1_1, response.protocol());
        }
        assertNotNull(response.body());
        assertEquals("GET /", response.body().string());
        assertEquals(response.header("content-type"), "text/plain");
    }

    @ParameterizedTest
    @ValueSource(strings = {"h2,http/1.1", "http/1.1"})
    void testSlashPost(String protocol) throws Exception {
        OkHttpClient client = OkHttpUnsafe.getUnsafeClient(protocol.split(","));
        Response response =
                client
                        .newCall(
                                new Request.Builder()
                                        .url("https://127.0.0.1:8443/")
                                        .post(RequestBody.create(MediaType.get("application/json"), "{}"))
                                        .build())
                        .execute();

        assertEquals(200, response.code());
        if (protocol.equals("h2,http/1.1")) {
            assertEquals(Protocol.HTTP_2, response.protocol());
        } else {
            assertEquals(Protocol.HTTP_1_1, response.protocol());
        }
        assertNotNull(response.body());
        assertEquals("<html><body>POST /</body></html>", response.body().string());
        assertEquals(response.header("content-type"), "text/html");
    }

    @ParameterizedTest
    @ValueSource(strings = {"h2,http/1.1", "http/1.1"})
    void testParamsGet(String protocol) throws Exception {
        OkHttpClient client = OkHttpUnsafe.getUnsafeClient(protocol.split(","));
        Response response =
                client
                        .newCall(
                                new Request.Builder().url("https://127.0.0.1:8443/foo/baz/bar/fiz").get().build())
                        .execute();

        assertEquals(200, response.code());
        if (protocol.equals("h2,http/1.1")) {
            assertEquals(Protocol.HTTP_2, response.protocol());
        } else {
            assertEquals(Protocol.HTTP_1_1, response.protocol());
        }
        assertNotNull(response.body());
        assertEquals("GET /foo/baz/bar/fiz", response.body().string());
        assertEquals(response.header("content-type"), "text/plain");
    }

    @ParameterizedTest
    @ValueSource(strings = {"h2,http/1.1", "http/1.1"})
    void testQueryParamsGet(String protocol) throws Exception {
        OkHttpClient client = OkHttpUnsafe.getUnsafeClient(protocol.split(","));
        Response response = client.newCall(
                new Request.Builder()
                        .url("https://127.0.0.1:8443/query?q=foo&q=bar&ans=42")
                        .get()
                        .build()
        ).execute();

        assertEquals(200, response.code());
        if (protocol.equals("h2,http/1.1")) {
            assertEquals(Protocol.HTTP_2, response.protocol());
        } else {
            assertEquals(Protocol.HTTP_1_1, response.protocol());
        }
        assertNotNull(response.body());
        Queries actual = new ObjectMapper().readValue(response.body().byteStream(), Queries.class);
        assertEquals(
                Queries.builder()
                        .queries(
                                Maps.of(
                                        "q", SetsKt.setOf("foo", "bar"),
                                        "ans", SetsKt.setOf("42")
                                )
                        )
                        .build(),
                actual
        );
        assertEquals(response.header("content-type"), "application/json");
    }

    @ParameterizedTest
    @ValueSource(strings = {"h2,http/1.1", "http/1.1"})
    void testParamsFileGet(String protocol) throws Exception {
        OkHttpClient client = OkHttpUnsafe.getUnsafeClient(protocol.split(","));
        Response response =
                client
                        .newCall(
                                new Request.Builder()
                                        .url("https://127.0.0.1:8443/styles/v1/willard/foo_map/sprite@2x.json")
                                        .get()
                                        .build())
                        .execute();

        assertEquals(200, response.code());
        if (protocol.equals("h2,http/1.1")) {
            assertEquals(Protocol.HTTP_2, response.protocol());
        } else {
            assertEquals(Protocol.HTTP_1_1, response.protocol());
        }
        assertNotNull(response.body());
        Sprite expected = Sprite.builder().user("willard").mapId("foo_map").build();
        Sprite actual = new ObjectMapper().readValue(response.body().byteStream(), Sprite.class);
        assertEquals(expected, actual);
        assertEquals("application/json", response.header("content-type"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"h2,http/1.1", "http/1.1"})
    void testUnknown(String protocol) throws Exception {
        OkHttpClient client = OkHttpUnsafe.getUnsafeClient(protocol.split(","));
        Response response =
                client
                        .newCall(new Request.Builder().url("https://127.0.0.1:8443/unknown").get().build())
                        .execute();

        assertEquals(404, response.code());
        if (protocol.equals("h2,http/1.1")) {
            assertEquals(Protocol.HTTP_2, response.protocol());
        } else {
            assertEquals(Protocol.HTTP_1_1, response.protocol());
        }
    }

    static class TestServer {
        ArrayList<String> receivedWsMessages = new ArrayList<>();

        Closeable start() throws Exception {
            return VialServer.Companion.create()
                    .httpGet("/", ((request, responseBuilder) -> responseBuilder.setBodyText("GET /")))
                    .httpPost(
                            "/",
                            ((request, responseBuilder) ->
                                    responseBuilder.setBodyHtml("<html><body>POST /</body></html>")))
                    .httpGet(
                            "/foo/:param1/bar/:param2",
                            ((request, responseBuilder) -> {
                                String param1 = request.pathParamOption("param1").orElse("unknown");
                                String param2 = request.pathParamOption("param2").orElse("unknown");
                                return responseBuilder.setBodyText(
                                        String.format("GET /foo/%s/bar/%s", param1, param2));
                            }))
                    .httpGet(
                            "/query",
                            ((request, responseBuilder) -> {
                                val builder = Queries.builder()
                                        .queries(new HashMap<>());

                                for (String k : request.queryKeys()) {
                                    for (String v : request.queryParams(k)) {
                                        builder.queries.computeIfAbsent(k, (qq) -> new HashSet<>()).add(v);
                                    }
                                }

                                return responseBuilder.setBodyJson(builder.build());
                            }))
                    .httpGet(
                            "/styles/v1/:user/:mapId/sprite@2x.json",
                            ((request, responseBuilder) -> {
                                String user = request.pathParamOption("user").orElse("unknown");
                                String mapId = request.pathParamOption("mapId").orElse("unknown");
                                return responseBuilder.setBodyJson(
                                        Sprite.builder().user(user).mapId(mapId).build());
                            }))
                    .webSocket("/websocket", webSocketSender -> {
                        webSocketSender.send(new WebSocketTextMessage("hello ws"));
                        return null;
                    }, webSocketMessage -> {
                        if (webSocketMessage instanceof WebSocketTextMessage) {
                            String msg = ((WebSocketTextMessage) webSocketMessage).getText();
                            receivedWsMessages.add(msg);
                        }
                        return null;
                    })
                    .listenAndServe()
                    .get();
        }
    }

    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class Sprite {
        @JsonProperty("user")
        private String user;

        @JsonProperty("map_id")
        private String mapId;
    }

    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class Queries {
        @JsonProperty("queries")
        private Map<String, Set<String>> queries;
    }
}
