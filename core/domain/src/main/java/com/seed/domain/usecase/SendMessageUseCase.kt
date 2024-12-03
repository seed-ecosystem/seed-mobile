package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.MessageEncodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.SendMessageDto

sealed interface SendMessageResult {
	data object Success : SendMessageResult

	data object Failure : SendMessageResult
}

class SendMessageUseCase(
	private val chatRepository: ChatRepository,
	private val seedCoder: SeedCoder,
	private val logger: Logger,
	private val getMessageKey: GetMessageKeyUseCase,
) {
	suspend operator fun invoke(
		chatId: String,
		messageAuthor: String,
		messageText: String,
		lastMessageNonce: Int,
	): SendMessageResult {
		logger.d(
			tag = "SendMessageUseCase",
			message = """
				Sending message with this data:
				- chatId $chatId
				- messageAuthor $messageAuthor
				- messageText $messageText
				- lastMessageNonce $lastMessageNonce
			""".trimIndent()
		)

		val messageKey = getMessageKey(
			chatId = chatId,
			nonce = lastMessageNonce
		)

		if (messageKey == null) {
			logger.e(tag = "SendMessageUseCase", message = "Error: message key is null")
			return SendMessageResult.Failure
		}

		val encodingOptions = MessageEncodeOptions(
			chatId = chatId,
			title = messageAuthor,
			text = messageText,
			previousKey = messageKey,
		)

		val encodingResult = seedCoder
			.encodeMessage(encodingOptions)

		if (encodingResult == null) return SendMessageResult.Failure

		val dto = SendMessageDto(
			chatId = chatId,
			nonce = lastMessageNonce + 1,
			encryptedContentBase64 = encodingResult.content,
			encryptedContentIv = encodingResult.contentIv,
			signature = encodingResult.signature,
		)

		chatRepository.sendMessage(dto)

		return SendMessageResult.Success
	}
}