package com.seed.domain.api

import com.seed.domain.model.ChatEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SeedMessagingApi {
	val chatEvents: SharedFlow<ChatEvent>
	val connectionState: StateFlow<SocketConnectionState>

	suspend fun launchConnection(coroutineScope: CoroutineScope)

	suspend fun stopConnection()

	suspend fun sendMessage(
		chatId: String,
		content: String,
		contentIv: String,
		nonce: Int,
		signature: String,
	): ApiResponse<Unit>

	suspend fun subscribeToChat(chatId: String, nonce: Int): ApiResponse<Unit>
}