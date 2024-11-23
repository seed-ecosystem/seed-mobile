package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.MessageEncodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.SendMessageDto
import kotlin.random.Random

class SendMessageUseCase(
	private val chatRepository: ChatRepository,
	private val seedCoder: SeedCoder,
	private val logger: Logger,
) {
	suspend operator fun invoke(
		chatId: String,
		messageAuthor: String,
		messageText: String,
	) {
		val chatKey = chatRepository.getChatKey(chatId)

		if (chatKey == null) {
			logger.e(tag = "SendMessageUseCase", message = "chat key is null")
			return
		}

		val encodingOptions = MessageEncodeOptions(
			chatId = chatId,
			title = messageAuthor,
			text = messageText,
			previousKey = chatKey,
		)

		val encodingResult = seedCoder
			.encodeMessage(encodingOptions)

		val dto = SendMessageDto(
			chatId = chatId,
			messageId = "messageId-${Random.nextInt()}", // todo
			encryptedContentBase64 = encodingResult?.content ?: "n/a",
			encryptedContentIv = encodingResult?.contentIv ?: "n/a"
		)

		chatRepository.sendMessage(dto)
	}
}