package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class SubscribeToChatUseCase( // TODO: that's all is temp and should be refactored
	private val chatRepository: ChatRepository,
	private val seedCoder: SeedCoder,
	private val logger: Logger,
) {
	suspend operator fun invoke(chatId: String): Flow<MessageContent> = chatRepository
		.getData(chatId)
		.map { update ->
			val chatKeyResult = chatRepository.getLastChatKey(chatId)

			if (chatKeyResult == null) {
				logger.e(tag = "SubscribeToChatUseCase", "chatKey is null")
				return@map MessageContent.UnknownMessage
			}

			val decodeOptions = DecodeOptions(
				content = update.encryptedContentBase64,
				contentIv = update.encryptedContentIv,
				signature = "", // TODO
				key = chatKeyResult.key
			)

			val decrypted = seedCoder.decodeChatUpdate(decodeOptions)

			return@map decrypted?.let {
				MessageContent.RegularMessage(
					nonce = Random.nextInt(),
					author = decrypted.title,
					text = decrypted.text,
				)
			} ?: MessageContent.UnknownMessage
		}
}