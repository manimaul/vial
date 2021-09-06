package com.willkamp.vial.api

import java.util.*
import java.util.function.Consumer

interface WebSocket {
    val uri: String?
    fun headers(): Iterable<Map.Entry<CharSequence, CharSequence>>
    fun queryParams(key: String): List<String>
    fun queryKeys(): Set<String>
    fun queryParam(key: String): String?
    fun queryParamOption(key: String): Optional<String> {
        return Optional.ofNullable(queryParam(key))
    }
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
