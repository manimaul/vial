package com.willkamp.vial.implementation

import com.willkamp.vial.api.ServerInitializer
import com.willkamp.vial.api.VialConfig
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import io.netty.channel.WriteBufferWaterMark
import java.io.Closeable
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.util.concurrent.CompletableFuture

internal open class NettyInitializer(
        private val initialHandler: ChannelHandler,
        private val channelConfig: ChannelConfig,
        override val vialConfig: VialConfig
) : ServerInitializer {

    private val log = logger()
    private var channelFuture: ChannelFuture? = null

    private fun serve(future: CompletableFuture<Closeable>?) {
        val bootstrap = ServerBootstrap()
        try {
            val address = InetAddress.getByName(vialConfig.address)
            val socketAddress = InetSocketAddress(address, vialConfig.port)
            bootstrap
                    .group(channelConfig.bossEventLoopGroup, channelConfig.eventLoopGroup)
                    .channel(channelConfig.channelClass)
                    .localAddress(socketAddress)
                    .childOption(ChannelOption.TCP_NODELAY, true) // turn off Nagle's Algo
                    .option(
                            ChannelOption.SO_BACKLOG,
                            vialConfig.maxConnBacklog) // max connection queue backlog size
                    .option(
                            ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            vialConfig.connTimeout) // connection timeout
                    .option( // write task queue high and low watermarks
                            ChannelOption.WRITE_BUFFER_WATER_MARK,
                            WriteBufferWaterMark(
                                    vialConfig.writeBufferQueueSizeBytesLow,
                                    vialConfig.writeBufferQueueSizeBytesHigh))
                    .childHandler(initialHandler)
            val cf = bootstrap.bind()
            channelFuture = cf
            cf.addListener {
                log.info("server listening on address $socketAddress")
                future?.complete(this)
            }
            cf.sync()
            cf.channel().closeFuture().sync()
            channelFuture = null
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            close()
        }
    }

    override fun listenAndServeBlocking() {
        serve(null)
    }

    override fun listenAndServe(): CompletableFuture<Closeable> {
        val future = CompletableFuture<Closeable>()
        Thread { serve(future) }.start()
        return future
    }

    override fun close() {
        try {
            channelFuture?.channel()?.close()
            channelConfig.eventLoopGroup.shutdownGracefully().sync()
        } catch (e: InterruptedException) {
            log.error("error", e)
            throw RuntimeException(e)
        }
    }
}