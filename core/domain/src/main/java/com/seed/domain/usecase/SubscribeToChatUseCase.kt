package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.ChatEvent
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

sealed interface DecodedChatEvent {
	data class New(
		val message: MessageContent.RegularMessage,
	) : DecodedChatEvent

	data class Unknown(
		val nonce: Int
	) : DecodedChatEvent

	data object Wait : DecodedChatEvent
}

class SubscribeToChatUseCase(
	private val chatRepository: ChatRepository,
	private val coder: SeedCoder,
	private val logger: Logger,
	private val getMessageKeyUseCase: GetMessageKeyUseCase,
) {
	private val chatId = CompletableDeferred<String>()

	val chatUpdatesSharedFlow = chatRepository
		.chatUpdatesSharedFlow
		.map { event ->
			logger.d(tag = "SubscribeToChatUseCase", message = "event = $event")
			return@map if (event is ChatEvent.New) {
				logger.d(tag = "SubscribeToChatUseCase", message = "nonce of new = ${event.nonce}")

				val decoded = decodeRegularMessageChatEvent(
					chatId = chatId.await(),
					event = event,
				)

				logger.d(tag = "SubscribeToChatUseCase", message = "decoded = $decoded")

				return@map decoded
			} else DecodedChatEvent.Wait
		}

	suspend operator fun invoke(chatId: String) {
		this.chatId.complete(chatId)

		chatRepository
			.subscribeToTheChat(chatId)
	}

	private suspend fun decodeRegularMessageChatEvent(
		chatId: String,
		event: ChatEvent.New
	): DecodedChatEvent {
		val chatKeyResult = chatRepository.getChatKey(chatId, event.nonce)

		val messageKey = chatKeyResult ?: getMessageKeyUseCase(
			chatId = chatId,
			nonce = event.nonce
		) ?: return DecodedChatEvent.Unknown(nonce = event.nonce)

		val decodeOptions = DecodeOptions(
			content = event.encryptedContentBase64,
			contentIv = event.encryptedContentIv,
			signature = event.signature,
			key = messageKey
		)

		val decodeResult = coder.decodeChatUpdate(decodeOptions)
			?: return DecodedChatEvent.Unknown(event.nonce)

		return DecodedChatEvent.New(
			message = MessageContent.RegularMessage(
				nonce = event.nonce,
				author = decodeResult.title,
				text = decodeResult.text,
			)
		)
	}
}