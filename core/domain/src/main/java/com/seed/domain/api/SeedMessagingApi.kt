package com.seed.domain.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetHistoryResponse(
	val type: String = "response",
	val messages: List<Message>
) {
	@Serializable
	data class Message(
		val chatId: String,
		val content: String,
		@SerialName("contentIV")
		val contentIv: String,
		val nonce: Int,
		val signature: String,
	)
}

sealed interface ApiResponse<T> {
	data class Success<T>(
		val data: T
	) : ApiResponse<T>

	data class Failure<T>(
		val message: String? = null
	) : ApiResponse<T>
}


interface SeedMessagingApi {
	suspend fun getHistory(
		chatId: String,
		amount: Int,
		nonce: Int?,
	): ApiResponse<GetHistoryResponse>

	suspend fun sendMessage(
		chatId: String,
		content: String,
		contentIv: String,
		nonce: Int,
		signature: String,
	): ApiResponse<Unit>
}