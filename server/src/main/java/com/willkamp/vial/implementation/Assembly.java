package com.willkamp.vial.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.willkamp.vial.api.VialServer;
import lombok.Getter;

public enum Assembly {
    instance;

    @Getter(lazy = true)
    private final VialConfig vialConfig = new VialConfig();

    @Getter(lazy = true)
    private final ObjectMapper objectMapper = objectMapper();

    SslContextFactory getSslContextFactory() {
        return new SslContextFactory(getVialConfig());
    }

    ChannelConfig getChannelConfig() {
        return new ChannelConfig();
    }


    VialChannelInitializer getVialChannelInitializer() {
        return new VialChannelInitializer(getSslContextFactory().createSslContext());
    }

    public VialServer getVialServer() {
        return new VialServerImpl(getVialConfig(), getChannelConfig(), getVialChannelInitializer());
    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
