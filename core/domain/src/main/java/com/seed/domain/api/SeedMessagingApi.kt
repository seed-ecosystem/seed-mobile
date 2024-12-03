package com.seed.domain.api

import com.seed.domain.model.ChatEvent
import kotlinx.coroutines.flow.SharedFlow

sealed interface ApiResponse<T> {
	data class Success<T>(
		val data: T
	) : ApiResponse<T>

	data class Failure<T>(
		val message: String? = null
	) : ApiResponse<T>
}

interface SeedMessagingApi {
	val chatEvents: SharedFlow<ChatEvent>

	suspend fun sendMessage(
		chatId: String,
		content: String,
		contentIv: String,
		nonce: Int,
		signature: String,
	): ApiResponse<Unit>

	suspend fun subscribeToChat(chatId: String, nonce: Int): ApiResponse<Unit>
}