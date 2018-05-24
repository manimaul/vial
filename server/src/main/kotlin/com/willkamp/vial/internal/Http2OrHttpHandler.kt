package com.willkamp.vial.internal

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.api.RequestHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.handler.ssl.ApplicationProtocolNames
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler
import org.slf4j.LoggerFactory

import java.util.function.Consumer

private val log = LoggerFactory.getLogger(Http2OrHttpHandler::class.java)

/**
 * Negotiates with the browser if HTTP2 or HTTP is going to be used. Once decided, the Netty
 * pipeline is setup with the correct handlers for the selected protocol.
 */
internal class Http2OrHttpHandler internal constructor(
        private val h1FallbackPipelineAssembler: (ChannelPipeline) -> Unit,
        private val handlers: ImmutableMap<String, RequestHandler>
) : ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {

    @Throws(Exception::class)
    override fun configurePipeline(ctx: ChannelHandlerContext, protocol: String) {
        if (ApplicationProtocolNames.HTTP_2 == protocol) {
            log.debug("configuring pipeline for h2")
            ctx.pipeline().addLast(RestHttp2HandlerBuilder(this.handlers).build())
            return
        }

        if (ApplicationProtocolNames.HTTP_1_1 == protocol) {
            log.debug("configuring pipeline for h1")
            h1FallbackPipelineAssembler(ctx.pipeline())
            return
        }

        throw IllegalStateException("unknown protocol: $protocol")
    }
}
