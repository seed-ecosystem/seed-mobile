package com.seed.api.models

import com.seed.domain.model.ApiEvent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("response")
data class ForwardingResponseStatus(
	val type: String,
	val status: Boolean,
)

@Serializable
internal sealed interface IncomingContent {
	@SerialName("type")
	val type: String

	@Serializable
	@SerialName("response")
	data class IncomingResponse(
		override val type: String,
		val response: Response,
	) : IncomingContent {
		@Serializable
		data class Response(
			val status: Boolean,
		)
	}

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
			val queueId: String,
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
		val queueId: String,
	) : EventContent

	@Serializable
	@SerialName("disconnected")
	data class Disconnected(
		override val type: String,
		val url: String,
	) : EventContent
}

internal fun IncomingContent.SubscribeEvent.toChatEvent(): ApiEvent {
	return when (this.event) {
		is EventContent.New -> {
			val newMessage = this.event.message

			ApiEvent.New(
				chatId = ChatId(newMessage.queueId),
				encryptedContentBase64 = newMessage.content,
				encryptedContentIv = newMessage.contentIV,
				nonce = ServerNonce(newMessage.nonce),
				signature = newMessage.signature
			)
		}

		is EventContent.Disconnected -> {
			ApiEvent.ServerDisconnect(
				url = ServerUrl(this.event.url)
			)
		}

		is EventContent.Wait -> {
			ApiEvent.Wait(ChatId(this.event.queueId))
		}
	}
}

