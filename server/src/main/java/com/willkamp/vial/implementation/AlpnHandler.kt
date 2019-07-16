package com.willkamp.vial.implementation

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.handler.ssl.ApplicationProtocolNames
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler
import java.util.function.Consumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class AlpnHandler(
        private val fallback: Consumer<ChannelPipeline>,
        private val routeRegistry: RouteRegistry,
        private val vialConfig: VialConfig
) : ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {

    private val log = logger<AlpnHandler>()

    @Throws(Exception::class)
    override fun configurePipeline(ctx: ChannelHandlerContext, protocol: String) {
        if (ApplicationProtocolNames.HTTP_2 == protocol) {
            log.debug("configuring pipeline for h2")
            ctx.pipeline().addLast(Http2HandlerBuilder(routeRegistry, vialConfig).build())
            return
        }

        if (ApplicationProtocolNames.HTTP_1_1 == protocol) {
            log.debug("configuring pipeline for h1")
            fallback.accept(ctx.pipeline())
            return
        }
        throw IllegalStateException(String.format("unknown protocol: %s", protocol))
    }
}
