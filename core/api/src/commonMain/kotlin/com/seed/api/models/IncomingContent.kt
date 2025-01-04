package com.seed.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface IncomingContent {
	@SerialName("type")
	val type: String

	@Serializable
	@SerialName("response")
	data class Response(
		override val type: String,
		val status: Boolean,
	) : IncomingContent

	@Serializable
	@SerialName("event")
	data class SubscribeEvent(
		override val type: String,
		val event: EventContent,
	) : IncomingContent
}

@Serializable
sealed interface EventContent {
	val type: String

	@Serializable
	@SerialName("new")
	data class New(
		override val type: String,
		val message: NewMessage,
	) : EventContent {
		@Serializable
		data class NewMessage(
			val chatId: String,
			val content: String,
			val contentIV: String,
			val nonce: Int,
			val signature: String,
		)
	}

	@Serializable
	@SerialName("wait")
	data class Wait(
		override val type: String,
		val chatId: String,
	) : EventContent
}