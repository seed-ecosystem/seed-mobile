package com.seed.domain.api

import com.seed.domain.model.ApiEvent
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SeedApi {
	val apiEvents: SharedFlow<ApiEvent>
	val connectionState: StateFlow<SocketConnectionState>

	fun launchConnection(coroutineScope: CoroutineScope)

	suspend fun stopConnection()

	suspend fun sendMessage(
		chatId: String,
		serverUrl: ServerUrl,
		content: String,
		contentIv: String,
		nonce: Int,
		signature: String,
	): ApiResponse<Unit>

	suspend fun subscribeToChat(chatId: String, nonce: Int, serverUrl: ServerUrl): ApiResponse<Unit>
}
