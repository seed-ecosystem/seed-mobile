package com.seed.domain.model

sealed interface ChatEvent {
	data class Stored(
		val messages: List<MessageContent>
	) : ChatEvent

	data class New(
		val encryptedContentBase64: String,
		val encryptedContentIv: String,
		val nonce: Int,
		val signature: String,
	) : ChatEvent

	data class Unknown(
		val nonce: Int,
	) : ChatEvent

	data object Wait : ChatEvent
}