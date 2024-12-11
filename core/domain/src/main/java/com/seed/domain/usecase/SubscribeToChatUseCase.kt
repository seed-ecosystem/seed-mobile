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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

sealed interface DecodedChatEvent {
	data class Stored(
		val messages: List<MessageContent>,
	) : DecodedChatEvent

	data class New(
		val message: MessageContent.RegularMessage,
	) : DecodedChatEvent

	data class Unknown(
		val nonce: Int
	) : DecodedChatEvent

	data object Wait : DecodedChatEvent

	data object Reconnection : DecodedChatEvent

	data object Connected : DecodedChatEvent

	data object Disconnected : DecodedChatEvent
}

class SubscribeToChatUseCase(
	private val chatRepository: ChatRepository,
	private val coder: SeedCoder,
	private val logger: Logger,
	private val getMessageKeyUseCase: GetMessageKeyUseCase,
) {
	suspend operator fun invoke(chatId: String, scope: CoroutineScope): Flow<DecodedChatEvent> {
		chatRepository.launchConnection(scope)

		chatRepository
			.subscribeToTheChat(scope, chatId, nonce = 1620)

		return chatRepository
			.chatUpdatesSharedFlow
			.map { event ->
				return@map when (event) {
					is ChatEvent.New -> {
						decodeRegularMessageChatEvent(
							chatId = chatId,
							event = event,
						)
					}

					is ChatEvent.Connected -> {
						chatRepository.subscribeToTheChat(
							coroutineScope = scope,
							chatId = chatId,
							nonce = 1620,
						)

						DecodedChatEvent.Connected
					}

					else -> DecodedChatEvent.Wait
				}
			}
	}

	private suspend fun decodeRegularMessageChatEvent(
		chatId: String,
		event: ChatEvent.New
	): DecodedChatEvent {
		val messageKey = getMessageKeyUseCase(
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