package com.willkamp.vial.api;

import com.willkamp.vial.implementation.Assembly;
import io.netty.handler.codec.http.HttpMethod;

import java.io.Closeable;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface VialServer {

    public static VialServer create() {
        return Assembly.instance.getVialServer();
    }

    VialServer request(HttpMethod method, String route, RequestHandler handler);

    VialServer staticContent(File rootDirectory);

    void listenAndServeBlocking();

    CompletableFuture<Closeable> listenAndServe();

    default VialServer options(String route, RequestHandler handler) {
        return request(HttpMethod.OPTIONS, route, handler);
    }

    default VialServer get(String route, RequestHandler handler) {
        return request(HttpMethod.GET, route, handler);
    }

    default VialServer head(String route, RequestHandler handler) {
        return request(HttpMethod.HEAD, route, handler);
    }

    default VialServer post(String route, RequestHandler handler) {
        return request(HttpMethod.POST, route, handler);
    }

    default VialServer put(String route, RequestHandler handler) {
        return request(HttpMethod.PUT, route, handler);
    }

    default VialServer patch(String route, RequestHandler handler) {
        return request(HttpMethod.PATCH, route, handler);
    }

    default VialServer delete(String route, RequestHandler handler) {
        return request(HttpMethod.DELETE, route, handler);
    }

    default VialServer trace(String route, RequestHandler handler) {
        return request(HttpMethod.TRACE, route, handler);
    }

    default VialServer connect(String route, RequestHandler handler) {
        return request(HttpMethod.CONNECT, route, handler);
    }
}
