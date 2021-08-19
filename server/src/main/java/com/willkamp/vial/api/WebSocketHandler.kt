package com.willkamp.vial.api

import java.util.function.Consumer

interface WebSocket {
    fun send(message: WebSocketMessage)
    fun sendText(message: String) {
        send(WebSocketTextMessage(message))
    }
    fun sendBin(message: ByteArray) {
        send(WebSocketBinMessage(message))
    }
    fun receive(receiver: Consumer<WebSocketMessage>)
    fun receiveText(receiver: Consumer<String?>) {
        receive {
            if (it is WebSocketTextMessage) {
                receiver.accept(it.text)
            }
        }
    }
    fun receiveBin(receiver: Consumer<ByteArray?>) {
        receive {
            if (it is WebSocketBinMessage) {
                receiver.accept(it.bin)
            }
        }
    }
}

sealed class WebSocketMessage
data class WebSocketTextMessage(
        val text: String?
) : WebSocketMessage()

class WebSocketBinMessage(
        val bin: ByteArray
) : WebSocketMessage()

object WebSocketClosed : WebSocketMessage()
