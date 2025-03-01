package com.seed.domain.api

import com.seed.domain.model.ApiEvent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
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
}
