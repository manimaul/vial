package com.willkamp.vial.api

import com.typesafe.config.ConfigFactory

class VialConfig {
    val port: Int
    val address: String
    val maxContentLength: Int
    val maxConnBacklog: Int
    val connTimeout: Int
    val writeBufferQueueSizeBytesLow: Int
    val writeBufferQueueSizeBytesHigh: Int

    init {
        val config = ConfigFactory.load().getConfig("vial")
        port = config.getInt("port")
        address = config.getString("address")
        maxContentLength = config.getInt("maxContentLength")
        maxConnBacklog = config.getInt("maxConnBacklog")
        connTimeout = config.getInt("connTimeout")
        writeBufferQueueSizeBytesLow = config.getInt("writeBufferQueueSizeBytesLow")
        writeBufferQueueSizeBytesHigh = config.getInt("writeBufferQueueSizeBytesHigh")
    }
}
