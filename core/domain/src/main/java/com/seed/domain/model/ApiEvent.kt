package com.seed.domain.model

import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl

sealed interface ApiEvent {
	data class New(
		val chatId: ChatId,
		val encryptedContentBase64: String,
		val encryptedContentIv: String,
		val nonce: ServerNonce,
		val signature: String,
	) : ApiEvent

	data class ServerDisconnect(
		val url: ServerUrl,
	) : ApiEvent

	data class Unknown(
		val nonce: ServerNonce,
	) : ApiEvent

	data object Reconnection : ApiEvent

	data object Connected : ApiEvent

	data object Disconnected : ApiEvent

	data class Wait(val chatId: ChatId) : ApiEvent
}
