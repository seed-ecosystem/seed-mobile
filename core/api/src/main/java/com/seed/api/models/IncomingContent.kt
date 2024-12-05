package com.seed.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface IncomingContent {
	val type: String

	@Serializable
	@SerialName("response")
	data class Response(
		override val type: String,
		val status: Boolean,
	) : IncomingContent

	@Serializable
	@SerialName("event")
	sealed interface SubscribeEvent : IncomingContent {
		override val type: String

		@SerialName("event")
		val subscribeEventContent: EventContent

		sealed interface EventContent {
			val type: String

			@Serializable
			@SerialName("new")
			data class New(
				override val type: String,
				val messageId: String,
				val encryptedContentBase64: String,
				val encryptedContentIv: String,
				val nonce: Int,
				val signature: String,
			) : EventContent

			@Serializable
			@SerialName("wait")
			data class Wait(
				override val type: String,
			) : EventContent
		}
	}
}