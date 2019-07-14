package com.willkamp.vial.implementation

import com.willkamp.vial.api.*
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.handler.codec.http.HttpMethod
import java.io.Closeable
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.util.concurrent.CompletableFuture
import lombok.extern.slf4j.Slf4j

@Slf4j
class VialServerImpl internal constructor(
        private val vialConfig: VialConfig,
        private val channelConfig: ChannelConfig,
        private val vialChannelInitializer: VialChannelInitializer,
        private val routeRegistry: RouteRegistry) : VialServer, Closeable {
    private var channelFuture: ChannelFuture? = null
    private val log = logger<VialServerImpl>()

    override fun request(method: HttpMethod, route: String, handler: Function2<Request, ResponseBuilder, ResponseBuilder>): VialServer {
        routeRegistry.registerRoute(method, route, handler)
        return this
    }

    override fun staticContent(rootDirectory: File): VialServer {
        log.error("static content noop - not implemented")
        return this
    }

    override fun listenAndServeBlocking() {
        serve(null)
    }

    override fun listenAndServe(): CompletableFuture<Closeable> {
        val future = CompletableFuture<Closeable>()
        Thread { serve(future) }.start()
        return future
    }

    private fun serve(future: CompletableFuture<Closeable>?) {
        val bootstrap = ServerBootstrap()
        try {
            val address = InetAddress.getByName(vialConfig.address)
            val socketAddress = InetSocketAddress(address, vialConfig.port)
            bootstrap
                    .group(channelConfig.eventLoopGroup)
                    .channel(channelConfig.channelClass)
                    .localAddress(socketAddress)
                    .childHandler(vialChannelInitializer)
            channelFuture = bootstrap.bind()
            channelFuture!!.addListener { f ->
                future?.complete(this)
            }
            channelFuture!!.sync()
            channelFuture!!.channel().closeFuture().sync()
            channelFuture = null
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            close()
        }
    }

    override fun close() {
        try {
            if (channelFuture != null) {
                channelFuture!!.channel().close()
            }
            channelConfig.eventLoopGroup.shutdownGracefully().sync()
        } catch (e: InterruptedException) {
            log.error("error", e)
            throw RuntimeException(e)
        }

    }
}
