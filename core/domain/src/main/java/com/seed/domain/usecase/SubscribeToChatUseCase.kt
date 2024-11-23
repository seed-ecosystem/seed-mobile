package com.seed.domain.usecase

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
) {
	suspend operator fun invoke(chatId: String): Flow<MessageContent> = chatRepository
		.getData(chatId)
		.map { update ->
			val decodeOptions = DecodeOptions(
				content = update.encryptedContentBase64,
				contentIv = update.encryptedContentIv,
				signature = "", // TODO
				key = chatRepository.getChatKey(chatId)
			)

			val decrypted = seedCoder.decode(decodeOptions)

			return@map decrypted?.let {
				MessageContent.RegularMessage(
					messageId = "${Random.nextInt()}",
					author = "author",
					text = it,
				)
			} ?: MessageContent.UnknownMessage
		}
}