package com.willkamp.vial.api

import com.google.common.collect.ImmutableMap
import com.willkamp.vial.internal.ChannelConfig
import com.willkamp.vial.internal.ServerChannelInitializer
import io.netty.bootstrap.ServerBootstrap
import io.netty.handler.codec.http2.Http2SecurityUtil
import io.netty.handler.ssl.*
import io.netty.handler.ssl.util.SelfSignedCertificate
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.security.cert.CertificateException
import javax.net.ssl.SSLException

private val log = LoggerFactory.getLogger(Server::class.java)

class Server(
        private val port: Int = 8080,
        private val useTls: Boolean = false,
        private val h2Capable: Boolean = false
) {

    private val handlers = mutableMapOf<String, RequestHandler>()

    @Throws(CertificateException::class, SSLException::class)
    private fun sslContext(): SslContext? {
        if (useTls) {
            val provider = if (OpenSsl.isAlpnSupported()) SslProvider.OPENSSL else SslProvider.JDK
            val selfSignedCertificate = SelfSignedCertificate()
            val protocolNames: Array<String> = if (h2Capable) {
                arrayOf(ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1)
            } else {
                arrayOf(ApplicationProtocolNames.HTTP_1_1)
            }
            return SslContextBuilder.forServer(selfSignedCertificate.key(), selfSignedCertificate.cert())
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
        } else {
            return null
        }
    }

    fun get(route: String, handler: RequestHandler) : Server {
        handlers["GET_$route"] = handler
        return this
    }

    fun post(route: String, handler: RequestHandler) : Server {
        handlers["POST_$route"] = handler
        return this
    }

    //todo: other methods

    @Throws(InterruptedException::class, CertificateException::class, SSLException::class)
    fun serve() {
        val config = ChannelConfig()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(config.eventLoopGroup)
                    .channel(config.channelClass)
                    .localAddress(InetSocketAddress(port))
                    .childHandler(ServerChannelInitializer(sslContext(), h2Capable, ImmutableMap.copyOf(handlers)))
            val channelFuture = bootstrap.bind().addListener {
                log.warn("starting to accept connections")
            }.sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            config.eventLoopGroup.shutdownGracefully().sync()
        }
    }
}
