package com.willkamp.vial.internal

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.api.RequestHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.ssl.SslContext
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(ServerChannelInitializer::class.java)

internal class ServerChannelInitializer(
        private val sslContext: SslContext?,
        private val h2Capable: Boolean,
        private val handlers: ImmutableMap<String, RequestHandler>
) : ChannelInitializer<SocketChannel>() {

    public override fun initChannel(ch: SocketChannel) {
        log.debug("initialize tls:{} h2:{}", sslContext != null, h2Capable)
        if (sslContext != null && h2Capable) {
            configureTlsH2(ch)
        } else {
            val pipeline = ch.pipeline()
            if (sslContext != null) {
                pipeline.addLast(sslContext.newHandler(ch.alloc()))
            }
            configureH1(ch.pipeline())
        }
    }

    private fun configureTlsH2(ch: SocketChannel) {
        ch.pipeline()
                .addLast(sslContext!!.newHandler(ch.alloc()), Http2OrHttpHandler({
                    this.configureH1(it)
                }, handlers))
    }

    private fun configureH1(pipeline: ChannelPipeline) {
        pipeline
                .addLast("server codec duplex", HttpServerCodec())
                .addLast("message size limit aggregator", HttpObjectAggregator(512 * 1024))
                .addLast("request handler", ChannelRequestInboundHandler(handlers))
    }
}
