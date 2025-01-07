package com.seed.domain

import com.seed.domain.api.SeedApi
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.model.ApiEvent
import com.seed.domain.model.MessageContent
import com.seed.domain.usecase.GetMessageKeyUseCase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface WorkerEvent {
	data class DeferredNewEvent(
		val chatId: String,
		val nonce: Int,
		val deferredEvent: Deferred<MessageContent>
	) : WorkerEvent

	data class Wait(val chatId: String) : WorkerEvent

	data class Unknown(
		val nonce: Int
	) : WorkerEvent

	data object Reconnection : WorkerEvent

	data object Connected : WorkerEvent

	data object Disconnected : WorkerEvent
}

interface SeedWorker {
	val connectionState: StateFlow<SocketConnectionState>
	val events: SharedFlow<WorkerEvent>

	fun initializeWorker()

	suspend fun sendMessage(
		chatId: String,
		messageContent: MessageContent.RegularMessage,
	)

	suspend fun subscribe(
		chatId: String,
		nonce: Int,
	)
}

fun SeedWorker(
	coder: SeedCoder,
	seedApi: SeedApi,
	keyManager: KeyManager,
	getScope: GetApplicationCoroutineScope,
	logger: Logger,
): SeedWorker {
	return object : SeedWorker {
		override val connectionState: StateFlow<SocketConnectionState> = seedApi.connectionState

		private val _events = MutableSharedFlow<WorkerEvent>()
		override val events: SharedFlow<WorkerEvent> = _events

		suspend fun decryptNewEvent(
			apiEvent: ApiEvent.New,
		): MessageContent {
			logger.d(
				tag = "SeedWorker",
				message = "start decoding"
			)

			val messageKey = keyManager.getKey(apiEvent.chatId, apiEvent.nonce)

			if (messageKey == null) {
				logger.d(tag = "SeedWorker", message = "chatKey is null for $apiEvent")
				return MessageContent.UnknownMessage(apiEvent.nonce)
			}

			val decodeResult = coder.decodeChatUpdate(
				content = apiEvent.encryptedContentBase64,
				contentIv = apiEvent.encryptedContentIv,
				signature = apiEvent.signature,
				key = messageKey,
			)

			if (decodeResult == null) {
				logger.d(tag = "SeedWorker", message = "Unable to decode $apiEvent")

				return MessageContent.UnknownMessage(apiEvent.nonce)
			}

			return MessageContent.RegularMessage(
				nonce = apiEvent.nonce,
				title = decodeResult.title,
				text = decodeResult.text
			)
		}

		suspend fun handleApiEvent(
			apiEvent: ApiEvent,
		) {
			when (apiEvent) {
				is ApiEvent.Connected -> _events.emit(WorkerEvent.Connected)

				is ApiEvent.Disconnected -> _events.emit(WorkerEvent.Disconnected)

				is ApiEvent.Reconnection -> _events.emit(WorkerEvent.Reconnection)

				is ApiEvent.Wait -> _events.emit(WorkerEvent.Wait(apiEvent.chatId))

				is ApiEvent.Unknown -> _events.emit(WorkerEvent.Unknown(apiEvent.nonce))

				is ApiEvent.New -> {
					val deferred = getScope().async(
						start = CoroutineStart.LAZY
					) {
						decryptNewEvent(apiEvent)
					}
					_events.emit(
						WorkerEvent.DeferredNewEvent(
							chatId = apiEvent.chatId,
							nonce = apiEvent.nonce,
							deferredEvent = deferred
						)
					)
				}
			}
		}

		override fun initializeWorker() {
			logger.d(
				tag = "SeedWorker",
				message = "initializeWorker"
			)

			getScope().launch {
				seedApi.apiEvents.collect { apiEvent ->
					handleApiEvent(apiEvent)
				}
			}
		}

		override suspend fun sendMessage(
			chatId: String,
			messageContent: MessageContent.RegularMessage
		) {
			// todo result return & implement different nonce attempts

			val messageKey = keyManager.getKey(chatId, messageContent.nonce) ?: return

			val encodingResult = coder.encodeMessage(
				chatId = chatId,
				title = messageContent.title,
				text = messageContent.text,
				previousKey = messageKey
			) ?: return

			seedApi.sendMessage(
				chatId = chatId,
				nonce = messageContent.nonce,
				content = encodingResult.content,
				contentIv = encodingResult.contentIv,
				signature = encodingResult.signature,
			)
		}

		override suspend fun subscribe(chatId: String, nonce: Int) {
			seedApi.subscribeToChat(chatId, nonce)
		}
	}
}
