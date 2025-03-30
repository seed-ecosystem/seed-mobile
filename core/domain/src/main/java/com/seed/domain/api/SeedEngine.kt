package com.seed.domain.api

import com.seed.domain.model.ApiEvent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

data class ForwardingState(
	val connections: List<Connection>
) {
	data class Connection(
		val url: ServerUrl,
	)
}

interface SeedEngine {
	val events: SharedFlow<ApiEvent>
	val connectionState: StateFlow<SocketConnectionState>
	val forwardingState: StateFlow<ForwardingState>

	fun launchConnection(scope: CoroutineScope)

	suspend fun stop()

	suspend fun sendMessage(
		chatId: ChatId,
		serverUrl: ServerUrl,
		content: String,
		contentIv: String,
		nonce: ServerNonce,
		signature: String,
	): ApiResponse<Unit>

	suspend fun subscribeToChat(
		chatId: ChatId,
		nonce: ServerNonce,
		serverUrl: ServerUrl,
	): ApiResponse<Unit>

	suspend fun sendPing(serverUrl: ServerUrl): ApiResponse<Unit>

	suspend fun connectServer(url: ServerUrl)
}
