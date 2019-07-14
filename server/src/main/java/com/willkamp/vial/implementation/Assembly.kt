package com.willkamp.vial.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.willkamp.vial.api.VialServer
import lombok.Getter

internal object Assembly {
    val vialConfig: VialConfig by lazy { VialConfig() }

    val objectMapper: ObjectMapper by lazy {
        ObjectMapper().apply {
            registerModule(ParameterNamesModule())
            registerModule(JavaTimeModule())
        }
    }

    val routeRegistry: RouteRegistry by lazy { RouteRegistry() }

    val sslContextFactory: SslContextFactory
        get() = SslContextFactory(vialConfig)

    val channelConfig: ChannelConfig
        get() = ChannelConfig()

    val vialChannelInitializer: VialChannelInitializer
        get() = VialChannelInitializer(sslContextFactory.createSslContext())

    val vialServer: VialServer
        get() = VialServerImpl(
                vialConfig, channelConfig, vialChannelInitializer, routeRegistry)
}
