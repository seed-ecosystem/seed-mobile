package com.seed.domain.usecase

import com.seed.domain.api.ApiResponse
import com.seed.domain.api.GetHistoryResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.model.MessageContent

class GetChatHistoryUseCase(
	private val api: SeedMessagingApi,
	private val coder: SeedCoder,
	private val getMessageKeyUseCase: GetMessageKeyUseCase,
) {
	suspend operator fun invoke(
		chatId: String = "bHKhl2cuQ01pDXSRaqq/OMJeDFJVNIY5YuQB2w7ve+c=", // TODO
		amount: Int = 50, // TODO change this
		nonce: Int = 0,
		chatKey: String = "/uwFt2yxHi59l26H9V8VTN3Kq+FtRewuWNfz1TNVcnM=", // todo
	): List<MessageContent>? {
		val response = api.getHistory(
			chatId = chatId,
			amount = amount,
			nonce = null //nonce
		)

		return if (response is ApiResponse.Success) {
			response.data
				.messages
				.map { message: GetHistoryResponse.Message ->
//					GetHistoryResponse.Message::toMessageContent
					val messageKey = getMessageKeyUseCase(
						chatId = chatId,
						nonce = message.nonce
					)

					if (messageKey == null) return@map MessageContent.UnknownMessage

					val decodeOptions = DecodeOptions(
						content = message.content,
						contentIv = message.contentIv,
						signature = message.signature,
						key = messageKey
					)

					println("decodeOptions: $decodeOptions")

					val decodeResult = coder.decodeChatUpdate(
						options = decodeOptions
					)

					if (decodeResult != null) {
						return@map MessageContent.RegularMessage(
							nonce = message.nonce,
							author = decodeResult.title,
							text = decodeResult.text,
						)
					}

					return@map MessageContent.UnknownMessage
				}
		} else null
	}
}

fun GetHistoryResponse.Message.toMessageContent(): MessageContent {
	return MessageContent.RegularMessage(
		nonce = this.nonce,
		author = this.contentIv, // todo
		text = this.content,
	)
}