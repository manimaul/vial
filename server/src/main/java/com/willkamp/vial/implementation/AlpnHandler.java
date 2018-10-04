package com.willkamp.vial.implementation;

import com.willkamp.vial.api.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

class AlpnHandler extends ApplicationProtocolNegotiationHandler {

    private Logger log = LoggerFactory.getLogger(AlpnHandler.class);
    private final Consumer<ChannelPipeline> fallback;
    private final Map<String, RequestHandler> handlers;

    AlpnHandler(Map<String, RequestHandler> handlers, Consumer<ChannelPipeline> fallback) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.handlers = handlers;
        this.fallback = fallback;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            log.debug("configuring pipeline for h2");
            ctx.pipeline()
                    .addLast(new Http2HandlerBuilder(this.handlers).build());
            return;
        }

        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            log.debug("configuring pipeline for h1");
            fallback.accept(ctx.pipeline());
            return;
        }

        throw new IllegalStateException(String.format("unknown protocol: %s", protocol));
    }
}
