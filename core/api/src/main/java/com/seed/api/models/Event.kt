package com.seed.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EventWrapper(
	val type: String,
	val event: RawChatEvent,
)

@Serializable
internal sealed interface RawChatEvent {
	val type: String

	@Serializable
	@SerialName("new")
	data class New(
		override val type: String,
		val message: Message
	) : RawChatEvent {
		@Serializable
		data class Message(
			val nonce: Int,
			val chatId: String,
			val signature: String,
			val content: String,
			val contentIV: String
		)
	}

	@Serializable
	@SerialName("wait")
	data class WaitEvent(
		val chatId: String
	) : RawChatEvent {
		override val type: String = "wait"
	}
}