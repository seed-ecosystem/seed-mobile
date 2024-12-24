package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.NicknameRepository
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
	private val nicknameRepository: NicknameRepository,
) {
	suspend operator fun invoke(
		chatId: String,
		messageText: String,
		lastMessageNonce: Int,
	): SendMessageResult {
		val author = nicknameRepository.getNickname().let {
			if (it.isNullOrEmpty()) "Anonymous android user" else it
		}

		logger.d(
			tag = "SendMessageUseCase",
			message = """
				Sending message with this data:
				- chatId $chatId
				- messageAuthor $author
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

		val encodingResult = seedCoder
			.encodeMessage(
				chatId = chatId,
				title = author,
				text = messageText,
				previousKey = messageKey,
			)

		if (encodingResult == null) return SendMessageResult.Failure

		val dto = SendMessageDto(
			chatId = chatId,
			nonce = lastMessageNonce + 1,
			encryptedContentBase64 = encodingResult.content,
			encryptedContentIv = encodingResult.contentIv,
			signature = encodingResult.signature,
		)

		val sendMessageResult = chatRepository.sendMessage(dto)

		return if (sendMessageResult is com.seed.domain.data.SendMessageResult.Success) {
			SendMessageResult.Success
		} else SendMessageResult.Failure
	}
}
