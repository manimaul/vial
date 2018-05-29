package com.willkamp.vial.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

@PublishedApi
internal val MAPPER: ObjectMapper = ObjectMapper()
        .registerModule(ParameterNamesModule())
        .registerModule(Jdk8Module())
        .registerModule(JavaTimeModule())