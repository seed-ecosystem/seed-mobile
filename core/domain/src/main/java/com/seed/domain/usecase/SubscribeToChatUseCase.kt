package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.ChatEvent
import com.seed.domain.model.DecodedChatEvent
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SubscribeToChatUseCase(
	private val chatRepository: ChatRepository,
	private val coder: SeedCoder,
	private val logger: Logger,
	private val getMessageKeyUseCase: GetMessageKeyUseCase,
) {
	suspend operator fun invoke(chatId: String, scope: CoroutineScope): Flow<DecodedChatEvent> {
		chatRepository.launchConnection(scope)

		val messages = chatRepository.getMessages(
			chatId = chatId
		)

		var lastEmittedNonce = getLastCachedNonce(messages)

		chatRepository
			.subscribeToTheChat(scope, chatId, nonce = lastEmittedNonce)

		val resultingFlow = flow {
			emit(DecodedChatEvent.Stored(messages))

			chatRepository
				.chatUpdatesSharedFlow
				.map { event ->
					return@map when (event) {
						is ChatEvent.New -> {
							lastEmittedNonce = event.nonce

							val decodedChatEvent = decodeRegularMessageChatEvent(
								chatId = chatId,
								event = event,
							)

							if (decodedChatEvent is DecodedChatEvent.New)
								chatRepository.addMessage(chatId, decodedChatEvent.message)

							decodedChatEvent
						}

						is ChatEvent.Connected -> {
							chatRepository.subscribeToTheChat(
								coroutineScope = scope,
								chatId = chatId,
								nonce = lastEmittedNonce,
							)

							DecodedChatEvent.Connected
						}

						is ChatEvent.Disconnected -> DecodedChatEvent.Disconnected

						is ChatEvent.Reconnection -> DecodedChatEvent.Reconnection

						is ChatEvent.Wait -> DecodedChatEvent.Wait

						is ChatEvent.Unknown -> {
							lastEmittedNonce = event.nonce

							DecodedChatEvent.Unknown(event.nonce)
						}
					}
				}
				.collect { emit(it) }
		}

		return resultingFlow
	}

	private fun getLastCachedNonce(messages: List<MessageContent>) =
		if (messages.isEmpty()) 0
		else messages.maxOf { if (it is MessageContent.RegularMessage) it.nonce else 0 }

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
				title = decodeResult.title,
				text = decodeResult.text,
			)
		)
	}
}