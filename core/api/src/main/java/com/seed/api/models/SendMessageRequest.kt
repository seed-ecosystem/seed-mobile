package com.seed.api.models

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
	val type: String,
	val message: Message
) {
	@Serializable
	data class Message(
		val chatId: String,
		val content: String,
		val contentIv: String,
		val nonce: Int,
		val signature: String,
	)
}