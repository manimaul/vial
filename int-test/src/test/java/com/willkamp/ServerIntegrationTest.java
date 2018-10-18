package com.willkamp;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.willkamp.vial.api.VialServer;
import java.io.Closeable;
import lombok.*;
import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerIntegrationTest {

  private Closeable server;
  private OkHttpClient client;

  @BeforeEach
  void beforeEach() throws Exception {
    server = new TestServer().start();
    client = OkHttpUnsafe.getUnsafeClient(Protocol.HTTP_2, Protocol.HTTP_1_1);
  }

  @AfterEach
  void afterEach() throws Exception {
    server.close();
  }

  @Test
  void testSlashGet() throws Exception {
    Response response =
        client
            .newCall(new Request.Builder().url("https://127.0.0.1:8443/").get().build())
            .execute();

    assertEquals(200, response.code());
    assertNotNull(response.body());
    assertEquals("GET /", response.body().string());
    assertEquals(response.header("content-type"), "text/plain");
  }

  @Test
  void testSlashPost() throws Exception {
    Response response =
        client
            .newCall(
                new Request.Builder()
                    .url("https://127.0.0.1:8443/")
                    .post(RequestBody.create(MediaType.get("application/json"), "{}"))
                    .build())
            .execute();

    assertEquals(200, response.code());
    assertNotNull(response.body());
    assertEquals("<html><body>POST /</body></html>", response.body().string());
    assertEquals(response.header("content-type"), "text/html");
  }

  @Test
  void testParamsGet() throws Exception {
    Response response =
        client
            .newCall(
                new Request.Builder().url("https://127.0.0.1:8443/foo/baz/bar/fiz").get().build())
            .execute();

    assertEquals(200, response.code());
    assertNotNull(response.body());
    assertEquals("GET /foo/baz/bar/fiz", response.body().string());
    assertEquals(response.header("content-type"), "text/plain");
  }

  @Test
  void testParamsFileGet() throws Exception {
    Response response =
        client
            .newCall(
                new Request.Builder()
                    .url("https://127.0.0.1:8443/styles/v1/willard/foo_map/sprite@2x.json")
                    .get()
                    .build())
            .execute();

    assertEquals(200, response.code());
    assertNotNull(response.body());
    Sprite expected = Sprite.builder().user("willard").mapId("foo_map").build();
    Sprite actual = new ObjectMapper().readValue(response.body().byteStream(), Sprite.class);
    assertEquals(expected, actual);
    assertEquals("application/json", response.header("content-type"));
  }

  @Test
  void testUnknown() throws Exception {
    Response response =
        client
            .newCall(new Request.Builder().url("https://127.0.0.1:8443/unknown").get().build())
            .execute();

    assertEquals(404, response.code());
  }

  static class TestServer {
    Closeable start() throws Exception {
      return VialServer.create()
          .get("/", ((request, responseBuilder) -> responseBuilder.setBodyText("GET /")))
          .post(
              "/",
              ((request, responseBuilder) ->
                  responseBuilder.setBodyHtml("<html><body>POST /</body></html>")))
          .get(
              "/foo/:param1/bar/:param2",
              ((request, responseBuilder) -> {
                String param1 = request.pathParam("param1").orElse("unknown");
                String param2 = request.pathParam("param2").orElse("unknown");
                return responseBuilder.setBodyText(
                    String.format("GET /foo/%s/bar/%s", param1, param2));
              }))
          .get(
              "/styles/v1/:user/:mapId/sprite@2x.json",
              ((request, responseBuilder) -> {
                String user = request.pathParam("user").orElse("unknown");
                String mapId = request.pathParam("mapId").orElse("unknown");
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
