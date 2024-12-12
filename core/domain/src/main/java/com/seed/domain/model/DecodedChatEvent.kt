package com.seed.domain.model

sealed interface DecodedChatEvent {
	data class Stored(
		val messages: List<MessageContent>,
	) : DecodedChatEvent

	data class New(
		val message: MessageContent.RegularMessage,
	) : DecodedChatEvent

	data class Unknown(
		val nonce: Int
	) : DecodedChatEvent

	data object Wait : DecodedChatEvent

	data object Reconnection : DecodedChatEvent

	data object Connected : DecodedChatEvent

	data object Disconnected : DecodedChatEvent
}