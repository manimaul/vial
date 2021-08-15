package com.willkamp.vial.implementation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.logger() : Logger {
    return LoggerFactory.getLogger(javaClass)
}
