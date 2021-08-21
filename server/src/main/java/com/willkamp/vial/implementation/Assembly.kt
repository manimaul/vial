package com.willkamp.vial.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.willkamp.vial.api.ServerInitializer
import com.willkamp.vial.api.VialConfig
import com.willkamp.vial.api.VialServer
import io.netty.channel.ChannelHandler

internal object Assembly {

    val objectMapper: ObjectMapper by lazy {
        ObjectMapper().apply {
            registerModule(ParameterNamesModule())
            registerModule(JavaTimeModule())
        }
    }


    private fun createVialChannelInitializer(vialConfig: VialConfig, sslContextFactory: SslContextFactory, routeRegistry: RouteRegistry): VialChannelInitializer {
        return VialChannelInitializer(sslContextFactory.createSslContext(), vialConfig, routeRegistry)
    }

    fun createVialServer(): VialServer {
        val routeRegistry = RouteRegistry()
        val config = VialConfig()
        return VialServerImpl(
                config,
                ChannelConfig(),
                createVialChannelInitializer(config, SslContextFactory(), routeRegistry),
                routeRegistry
        )
    }

    fun createCustomInitializer(channelInitializer: ChannelHandler): ServerInitializer {
        return NettyInitializer(channelInitializer, ChannelConfig(), VialConfig())
    }
}
