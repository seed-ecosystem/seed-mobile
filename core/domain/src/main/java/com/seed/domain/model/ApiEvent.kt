package com.seed.domain.model

sealed interface ApiEvent {
	data class New(
		val chatId: String,
		val encryptedContentBase64: String,
		val encryptedContentIv: String,
		val nonce: Int,
		val signature: String,
	) : ApiEvent

	data class Unknown(
		val nonce: Int,
	) : ApiEvent

	data object Reconnection : ApiEvent

	data object Connected : ApiEvent

	data object Disconnected : ApiEvent

	data class Wait(val chatId: String) : ApiEvent
}
