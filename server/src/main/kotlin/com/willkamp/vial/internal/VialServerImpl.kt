package com.willkamp.vial.internal

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.api.RequestHandler
import com.willkamp.vial.api.TlsContext
import io.netty.bootstrap.ServerBootstrap
import io.netty.handler.codec.http2.Http2SecurityUtil
import io.netty.handler.ssl.*
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.InetSocketAddress
import java.security.cert.CertificateException
import java.util.concurrent.CompletableFuture
import javax.net.ssl.SSLException

private val log = LoggerFactory.getLogger(VialServerImpl::class.java)

internal class VialServerImpl (
        private val port: Int = 8080,
        private val h2Capable: Boolean = false,
        private val tlsContext: TlsContext?,
        private val handlers: ImmutableMap<String, RequestHandler>,
        private val config: ChannelConfig = ChannelConfig()
) : Closeable {

    @Throws(CertificateException::class, SSLException::class)
    private fun sslContext(): SslContext? {
        return tlsContext?.let {
            val provider = if (OpenSsl.isAlpnSupported()) SslProvider.OPENSSL else SslProvider.JDK
            val protocolNames: Array<String> = if (h2Capable) {
                arrayOf(ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1)
            } else {
                arrayOf(ApplicationProtocolNames.HTTP_1_1)
            }
            return SslContextBuilder.forServer(it.privateKey, *it.keyCertChain)
                    .sslProvider(provider)
                    /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                     * Please refer to the HTTP/2 specification for cipher requirements.
                     */
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.ALPN,
                            // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            *protocolNames))
                    .build()
        }
    }

    override fun close() {
        config.eventLoopGroup.shutdownGracefully().sync()
    }

    @Throws(InterruptedException::class, CertificateException::class, SSLException::class)
    fun serve(future: CompletableFuture<Closeable>? = null) {
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(config.eventLoopGroup)
                    .channel(config.channelClass)
                    .localAddress(InetSocketAddress(port))
                    .childHandler(ServerChannelInitializer(sslContext(), h2Capable, handlers))
            val channelFuture = bootstrap.bind().addListener {
                future?.complete(this)
                log.warn("starting to accept connections")
            }.sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            config.eventLoopGroup.shutdownGracefully().sync()
        }
    }
}
