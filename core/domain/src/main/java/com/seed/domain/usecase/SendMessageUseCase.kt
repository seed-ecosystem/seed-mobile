package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.EncodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.SendMessageDto

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

		val encodingOptions = EncodeOptions(
			content = "$messageAuthor $messageText", // todo
			key = chatKey
		)

		val encodingResult = seedCoder.encode(encodingOptions)

		val dto = SendMessageDto(
			chatId = chatId,
			messageId = "messageId", // todo
			encryptedContentBase64 = encodingResult?.content ?: "n/a",
			encryptedContentIv = encodingResult?.contentIv ?: "n/a"
		)

		chatRepository.sendMessage(dto)
	}
}