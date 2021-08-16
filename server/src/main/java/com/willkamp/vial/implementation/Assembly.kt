package com.willkamp.vial.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.willkamp.vial.api.VialServer

internal object Assembly {
    private fun createConfig(): VialConfig {
        return VialConfig()
    }

    val objectMapper: ObjectMapper by lazy {
        ObjectMapper().apply {
            registerModule(ParameterNamesModule())
            registerModule(JavaTimeModule())
        }
    }

    private fun createRoutRegistry(): RouteRegistry {
        return RouteRegistry()
    }

    private fun createSslContextFactory(): SslContextFactory {
        return SslContextFactory()
    }

    private val channelConfig: ChannelConfig
        get() = ChannelConfig()

    private fun createVialChannelInitializer(vialConfig: VialConfig, sslContextFactory: SslContextFactory, routeRegistry: RouteRegistry): VialChannelInitializer {
        return VialChannelInitializer(sslContextFactory.createSslContext(), vialConfig, routeRegistry)
    }

    val vialServer: VialServer
        get() {
            val routeRegistry = createRoutRegistry()
            val config = createConfig()
            val sslFactory = createSslContextFactory()
            return VialServerImpl(config, channelConfig, createVialChannelInitializer(config, sslFactory, routeRegistry), routeRegistry)
        }
}
