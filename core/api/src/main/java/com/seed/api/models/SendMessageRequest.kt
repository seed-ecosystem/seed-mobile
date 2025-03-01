package com.seed.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
	val type: String,
	val message: Message
) {
	@Serializable
	data class Message(
		val queueId: String,
		val content: String,
		@SerialName("contentIV")
		val contentIv: String,
		val nonce: Int,
		val signature: String,
	)

	companion object {
		fun createSendMessageRequest(
			chatId: String,
			content: String,
			contentIv: String,
			nonce: Int,
			signature: String,
		) = SendMessageRequest(
			type = "send",
			message = Message(
				queueId = chatId,
				content = content,
				contentIv = contentIv,
				nonce = nonce,
				signature = signature,
			)
		)
	}
}
