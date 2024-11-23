package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class SubscribeToChatUseCase(
	private val chatRepository: ChatRepository,
	private val seedCoder: SeedCoder,
	private val logger: Logger,
) {
	suspend operator fun invoke(chatId: String): Flow<MessageContent> = chatRepository
		.getData(chatId)
		.map { update ->
			val chatKey = chatRepository.getChatKey(chatId)

			if (chatKey == null) {
				logger.e(tag = "SubscribeToChatUseCase", "chatKey is null")
				return@map MessageContent.UnknownMessage
			}

			val decodeOptions = DecodeOptions(
				content = update.encryptedContentBase64,
				contentIv = update.encryptedContentIv,
				signature = "", // TODO
				key = chatKey
			)

			val decrypted = seedCoder.decodeChatUpdate(decodeOptions)

			return@map decrypted?.let {
				MessageContent.RegularMessage(
					messageId = "${Random.nextInt()}",
					author = decrypted.title,
					text = decrypted.text,
				)
			} ?: MessageContent.UnknownMessage
		}
}