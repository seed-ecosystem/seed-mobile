package com.seed.api.util

sealed interface SocketEvent {
	data class IncomingContent(
		val content: String,
	) : SocketEvent

	data object Reconnection : SocketEvent

	data object Connected : SocketEvent

	data object Disconnected : SocketEvent
}