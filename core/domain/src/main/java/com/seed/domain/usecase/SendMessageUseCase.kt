package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.NicknameRepository
import com.seed.domain.data.SendMessageDto

sealed interface SendMessageResult {
	/**
	 * @property newServerNonce Server nonce with which the message was sent
	 */
	data class Success(
		val newServerNonce: Int
	) : SendMessageResult

	data object Failure : SendMessageResult
}

class SendMessageUseCase(
	private val chatRepository: ChatRepository,
	private val seedCoder: SeedCoder,
	private val logger: Logger,
	private val getMessageKey: GetMessageKeyUseCase,
	private val nicknameRepository: NicknameRepository,
	private val nonceAttempts: Int,
) {
	suspend operator fun invoke(
		chatId: String,
		messageText: String,
	): SendMessageResult {
		val author = nicknameRepository.getNickname().let {
			if (it.isNullOrEmpty()) "Anonymous android user" else it
		}

		val lastMessageNonce = chatRepository
			.getMessages(chatId)
			.maxOf { it.nonce }

		val tillNonce = lastMessageNonce + nonceAttempts
		val startNonce = lastMessageNonce + 1

		for (currentNonce in startNonce..tillNonce) {
			val previousNonce = currentNonce - 1

			logger.d(
				tag = "SendMessageUseCase",
				message = """
				Trying to send message with this data:
				- chatId $chatId
				- messageAuthor $author
				- messageText $messageText
				- nonce $currentNonce
			""".trimIndent()
			)

			val previousMessageKey = getMessageKey(
				chatId = chatId,
				nonce = previousNonce
			)

			if (previousMessageKey == null) {
				logger.e(
					tag = "SendMessageUseCase",
					message = "Error sending message: message key is null"
				)
				return SendMessageResult.Failure
			}

			val encodingResult = seedCoder
				.encodeMessage(
					chatId = chatId,
					title = author,
					text = messageText,
					previousKey = previousMessageKey,
				)

			if (encodingResult == null) return SendMessageResult.Failure

			val dto = SendMessageDto(
				chatId = chatId,
				nonce = currentNonce,
				encryptedContentBase64 = encodingResult.content,
				encryptedContentIv = encodingResult.contentIv,
				signature = encodingResult.signature,
			)

			val sendMessageResult = chatRepository.sendMessage(dto)

			if (sendMessageResult is com.seed.domain.data.SendMessageResult.Success)
				return SendMessageResult.Success(currentNonce)
		}

		return SendMessageResult.Failure
	}
}
