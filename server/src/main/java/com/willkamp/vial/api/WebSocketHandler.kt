package com.willkamp.vial.api

typealias WebSocketReceiver = (WebSocketMessage) -> Unit
typealias WebSocketHandlerInit = (WebSocketSender) -> Unit

interface WebSocketSender {
    fun send(webSocketMessage: WebSocketMessage)
}

sealed class WebSocketMessage
data class WebSocketTextMessage(
        val text: String?
) : WebSocketMessage()

class WebSocketBinMessage(
        val bin: ByteArray
) : WebSocketMessage()

object WebSocketClosed : WebSocketMessage()
