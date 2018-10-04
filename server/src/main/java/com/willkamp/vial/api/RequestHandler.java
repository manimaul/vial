package com.willkamp.vial.api;

@FunctionalInterface
public interface RequestHandler {
    ResponseBuilder handle(Request request, ResponseBuilder responseBuilder) throws Exception;
}
