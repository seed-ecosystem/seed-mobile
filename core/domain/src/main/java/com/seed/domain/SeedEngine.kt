package com.seed.domain

import com.seed.domain.api.SocketConnectionState
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface EngineEvent {
	data class IncomingContent(
		val content: String,
		val url: ServerUrl,
	) : EngineEvent

	data object Reconnection : EngineEvent

	data object Connected : EngineEvent

	data object Disconnected : EngineEvent
}

enum class SocketSendResult {
	SUCCESS,
	FAILURE,
}

data class ForwardingState(
	val connections: List<Connection>
) {
	data class Connection(
		val url: ServerUrl,
	)
}

interface SeedEngine {
	val events: SharedFlow<EngineEvent>
	val connectionState: StateFlow<SocketConnectionState>
	val forwardingState: StateFlow<ForwardingState>

	fun initialize(scope: CoroutineScope)

	suspend fun stop()

	suspend fun send(
		serverUrl: ServerUrl,
		jsonRequest: String,
	)

	suspend fun connectServer(url: ServerUrl)
}

