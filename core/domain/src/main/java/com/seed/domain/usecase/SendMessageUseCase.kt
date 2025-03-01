package com.seed.domain.usecase

import com.seed.domain.KeyManager
import com.seed.domain.Logger
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.api.ApiResponse
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.data.SettingsRepository
import com.seed.domain.data.SendMessageDto
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl

sealed interface SendMessageResult {
	/**
	 * @property newServerNonce Server nonce with which the message was sent
	 */
	data class Success(
		val newServerNonce: ServerNonce
	) : SendMessageResult

	data object Failure : SendMessageResult
}

class SendMessageUseCase(
	private val chatRepository: ChatRepository,
	private val chatsRepository: ChatsRepository,
	private val seedWorkerStateHandle: SeedWorkerStateHandle,
	private val seedCoder: SeedCoder,
	private val logger: Logger,
	private val keyManager: KeyManager,
	private val settingsRepository: SettingsRepository,
	private val nonceAttempts: Int,
) {
	suspend operator fun invoke(
		chatId: ChatId,
		messageText: String,
	): SendMessageResult {
		val author = settingsRepository.getNickname().let {
			if (it.isNullOrEmpty()) "Anonymous android user" else it
		}

		val serverUrl = chatsRepository.getChatServerUrl(chatId)

		val lastMessageNonce = chatRepository
			.getMessages(chatId)
			.maxOfOrNull { it.nonce.value }

		if (lastMessageNonce == null) {
			return sendMessageWhenNoLastMessage(chatId, author, messageText, serverUrl)
		}

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

			val messageKey = keyManager.getKey(
				chatId = chatId,
				nonce = ServerNonce(previousNonce),
			)

			if (messageKey == null) {
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
					previousKey = messageKey,
				)

			if (encodingResult == null) return SendMessageResult.Failure

			val dto = SendMessageDto(
				chatId = chatId,
				nonce = ServerNonce(currentNonce),
				encryptedContentBase64 = encodingResult.content,
				encryptedContentIv = encodingResult.contentIv,
				signature = encodingResult.signature,
				serverUrl = serverUrl,
			)

			val sendMessageResult = seedWorkerStateHandle.sendMessage(dto)

			if (sendMessageResult is com.seed.domain.data.SendMessageResult.Success)
				return SendMessageResult.Success(ServerNonce(currentNonce))
		}

		return SendMessageResult.Failure
	}

	private suspend fun sendMessageWhenNoLastMessage(
		chatId: ChatId,
		author: String,
		messageText: String,
		serverUrl: ServerUrl
	): SendMessageResult {
		val messageNonce = chatsRepository
			.getChat(chatId)
			?.firstChatKeyNonce

		if (messageNonce == null) return SendMessageResult.Failure

		val messageKey = keyManager.getKey(
			chatId = chatId,
			nonce = messageNonce,
		)

		if (messageKey == null) return SendMessageResult.Failure

		val encodingResult = seedCoder
			.encodeMessageWithKey(
				chatId = chatId,
				title = author,
				text = messageText,
				messageKey = messageKey,
			)

		if (encodingResult == null) return SendMessageResult.Failure

		val dto = SendMessageDto(
			chatId = chatId,
			nonce = messageNonce,
			encryptedContentBase64 = encodingResult.content,
			encryptedContentIv = encodingResult.contentIv,
			signature = encodingResult.signature,
			serverUrl = serverUrl,
		)

		val sendMessageResult = seedWorkerStateHandle.sendMessage(dto)

		if (sendMessageResult is com.seed.domain.data.SendMessageResult.Success)
			return SendMessageResult.Success(messageNonce)

		return SendMessageResult.Failure
	}
}
