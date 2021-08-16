package com.willkamp.vial.implementation

import com.typesafe.config.ConfigFactory

internal class VialConfig {
    val port: Int
    val address: String
    val maxContentLength: Int

    init {
        val config = ConfigFactory.load().getConfig("vial")
        port = config.getInt("port")
        address = config.getString("address")
        maxContentLength = config.getInt("maxContentLength")
    }
}
