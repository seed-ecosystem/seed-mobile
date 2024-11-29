package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.MessageEncodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.SendMessageDto

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
	) {
//		val chatKey = chatRepository.getChatKey(chatId)
//
//		if (chatKey == null) {
//			logger.e(tag = "SendMessageUseCase", message = "chat key is null")
//			return
//		}

		logger.d(
			tag = "SendMessageUseCase",
			message = """
				chatId $chatId
				messageAuthor $messageAuthor
				messageText $messageText
				lastMessageNonce $lastMessageNonce
			""".trimIndent()
		)

		val messageKey = getMessageKey(
			chatId = chatId,
			nonce = lastMessageNonce
		)

		if (messageKey == null) {
			logger.e(tag = "SendMessageUseCase", message = "message key is null")
			return
		}

		val encodingOptions = MessageEncodeOptions(
			chatId = chatId,
			title = messageAuthor,
			text = messageText,
			previousKey = messageKey,
		)

		val encodingResult = seedCoder
			.encodeMessage(encodingOptions)

		val dto = SendMessageDto(
			chatId = chatId,
			nonce = lastMessageNonce + 1,
			encryptedContentBase64 = encodingResult?.content ?: "n/a",
			encryptedContentIv = encodingResult?.contentIv ?: "n/a",
			signature = encodingResult?.signature ?: "n/a", // todo resolve these n/a's
		)

		chatRepository.sendMessage(dto)
	}
}