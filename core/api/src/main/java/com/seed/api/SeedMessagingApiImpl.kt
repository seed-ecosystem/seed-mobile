package com.seed.api

import com.seed.api.models.EventContent
import com.seed.api.models.IncomingContent
import com.seed.api.models.SendMessageRequest
import com.seed.api.models.SubscribeRequest
import com.seed.api.util.SeedSocket
import com.seed.api.util.SocketEvent
import com.seed.api.util.SocketSendResult
import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.model.ChatEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun createSeedMessagingApi(logger: Logger, socket: SeedSocket): SeedMessagingApi {
	val responseQueue: MutableList<(IncomingContent.Response) -> Unit> = mutableListOf()

	val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
		logger.e(
			tag = "SeedMessagingApi",
			message = "An unhandled error occured: ${throwable.message.toString()}"
		)
	}

	val coroutineContext: CoroutineContext = Dispatchers.IO + Job() + coroutineExceptionHandler

	return object : SeedMessagingApi {
		private val _chatEvents = MutableSharedFlow<ChatEvent>()
		override val chatEvents: SharedFlow<ChatEvent> = _chatEvents

		override val connectionState: StateFlow<SocketConnectionState> = socket.connectionState

		override suspend fun launchConnection(coroutineScope: CoroutineScope) {
			coroutineScope.launch {
				socket.initializeSocketConnection(coroutineScope + coroutineContext)

				socket.socketConnectionEvents.collect { socketEvent ->
					when (socketEvent) {
						is SocketEvent.IncomingContent -> {
							val incomingMessage = parseSocketEvent(socketEvent)

							if (incomingMessage is IncomingContent.Response) {
								if (responseQueue.size > 0) {
									responseQueue[0](incomingMessage)
									responseQueue.removeAt(0)
								}
							}

							if (incomingMessage is IncomingContent.SubscribeEvent) {
								_chatEvents.emit(
									incomingMessage.toChatEvent()
								)
							}
						}

						is SocketEvent.Reconnection -> {
							_chatEvents.emit(ChatEvent.Reconnection)
						}

						SocketEvent.Connected -> _chatEvents.emit(ChatEvent.Connected)

						SocketEvent.Disconnected -> _chatEvents.emit(ChatEvent.Disconnected)
					}
				}
			}
		}

		override suspend fun stopConnection() {
			socket.disconnect()
		}

		private fun parseSocketEvent(incomingContent: SocketEvent.IncomingContent): IncomingContent? {
			return try {
				Json.decodeFromString<IncomingContent>(incomingContent.content)
			} catch (ex: SerializationException) {
				logger.e("SeedMessagingApi", "parseSocketEvent: Parsing error: ${ex.message}")
				null
			}
		}

		override suspend fun sendMessage(
			chatId: String,
			content: String,
			contentIv: String,
			nonce: Int,
			signature: String
		): ApiResponse<Unit> {
			val jsonRequest = Json.encodeToString(
				SendMessageRequest.createSendMessageRequest(
					chatId = chatId,
					content = content,
					contentIv = contentIv,
					nonce = nonce,
					signature = signature
				)
			)

			val socketSendResult = socket.send(jsonRequest)

			if (socketSendResult == SocketSendResult.FAILURE) {
				return ApiResponse.Failure()
			}

			logger.d(
				tag = "SeedMessagingApi",
				message = "sendMessage: Sent json: $jsonRequest"
			)

			return suspendCoroutine { continuation ->
				responseQueue.add { response ->
					logger.d(
						tag = "SeedMessagingApi",
						message = "sendMessage: Response: $response",
					)

					if (response.status) continuation.resume(ApiResponse.Success(Unit))
					else continuation.resume(ApiResponse.Failure())
				}
			}
		}

		override suspend fun subscribeToChat(chatId: String, nonce: Int): ApiResponse<Unit> {
				val subscribeRequest = SubscribeRequest(
					type = "subscribe",
					chatId = chatId,
					nonce = nonce
				)
				val jsonRequest = Json.encodeToString(subscribeRequest)

				val socketSendResult = socket.send(jsonRequest)

				if (socketSendResult == SocketSendResult.FAILURE) {
					return ApiResponse.Failure()
				}

				logger.d(
					tag = "SeedMessagingApi",
					message = "subscribeToChat: Sent $jsonRequest"
				)

				return suspendCoroutine { continuation ->
					responseQueue.add { response ->
						logger.d(
							tag = "SeedMessagingApi",
							message = "subscribeToChat: Got response: $response"
						)

						if (response.status) continuation.resume(ApiResponse.Success(Unit))
						else continuation.resume(ApiResponse.Failure())
					}
				}
			}
	}
}

private fun IncomingContent.SubscribeEvent.toChatEvent(): ChatEvent {
	return when (this.event) {
		is EventContent.New -> {
			val newMessage = this.event.message

			ChatEvent.New(
				encryptedContentBase64 = newMessage.content,
				encryptedContentIv = newMessage.contentIV,
				nonce = newMessage.nonce,
				signature = newMessage.signature
			)
		}

		is EventContent.Wait -> {
			ChatEvent.Wait
		}
	}
}
