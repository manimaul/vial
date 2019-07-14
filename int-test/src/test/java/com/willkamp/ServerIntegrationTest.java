package com.willkamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.willkamp.vial.api.VialServer;
import java.io.Closeable;
import lombok.*;
import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ServerIntegrationTest {

  private Closeable server;

  @BeforeEach
  void beforeEach() throws Exception {
    server = new TestServer().start();
  }

  @AfterEach
  void afterEach() throws Exception {
    server.close();
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
              "/styles/v1/:user/:mapId/sprite@2x.json",
              ((request, responseBuilder) -> {
                String user = request.pathParamOption("user").orElse("unknown");
                String mapId = request.pathParamOption("mapId").orElse("unknown");
                return responseBuilder.setBodyJson(
                    Sprite.builder().user(user).mapId(mapId).build());
              }))
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
}
