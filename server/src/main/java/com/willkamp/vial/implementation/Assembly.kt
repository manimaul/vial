package com.willkamp.vial.implementation

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.willkamp.vial.api.ServerInitializer
import com.willkamp.vial.api.VialConfig
import com.willkamp.vial.api.VialServer
import io.netty.channel.ChannelHandler

internal object Assembly {

    val objectMapper: JsonMapper by lazy {
        jsonMapper {
            addModule(kotlinModule())
        }
    }

    private fun createVialChannelInitializer(vialConfig: VialConfig, sslContextFactory: SslContextFactory, routeRegistry: RouteRegistry): VialChannelInitializer {
        val sslContext = if (vialConfig.useTls) sslContextFactory.createSslContext() else null
        return VialChannelInitializer(sslContext, vialConfig, routeRegistry)
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
