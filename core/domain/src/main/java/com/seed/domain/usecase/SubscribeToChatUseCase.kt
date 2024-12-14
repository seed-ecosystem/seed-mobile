package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.ChatEvent
import com.seed.domain.model.DecodedChatEvent
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

		var waitTriggered = false
		val decodedMessageDefers = mutableListOf<Deferred<DecodedChatEvent>>()

		val resultingFlow = channelFlow {
			send(DecodedChatEvent.Stored(messages))

			chatRepository
				.chatUpdatesSharedFlow
				.collect { event ->
					when (event) {
						is ChatEvent.New -> {
							lastEmittedNonce = event.nonce

							if (!waitTriggered) {
								val defer =
									scope.async {
										val decodedChatEvent = decodeRegularMessageChatEvent(
											chatId = chatId,
											event = event,
										)

										if (decodedChatEvent is DecodedChatEvent.New)
											chatRepository.addMessage(
												chatId,
												decodedChatEvent.message
											)

										return@async decodedChatEvent
									}

								decodedMessageDefers.add(defer)
							} else {
								val decodedChatEvent = decodeRegularMessageChatEvent(
									chatId = chatId,
									event = event,
								)

								if (decodedChatEvent is DecodedChatEvent.New)
									chatRepository.addMessage(
										chatId,
										decodedChatEvent.message
									)

								send(decodedChatEvent)
							}
						}

						is ChatEvent.Connected -> {
							waitTriggered = false

							chatRepository.subscribeToTheChat(
								coroutineScope = scope,
								chatId = chatId,
								nonce = lastEmittedNonce,
							)

							send(DecodedChatEvent.Connected)
						}

						is ChatEvent.Disconnected -> send(DecodedChatEvent.Disconnected)

						is ChatEvent.Reconnection -> send(DecodedChatEvent.Reconnection)

						is ChatEvent.Wait -> {
							decodedMessageDefers.awaitAll().forEach {
								send(it)
							}

							send(DecodedChatEvent.Wait)

							waitTriggered = true
						}

						is ChatEvent.Unknown -> {
							lastEmittedNonce = event.nonce

							send(DecodedChatEvent.Unknown(event.nonce))
						}
					}
				}
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