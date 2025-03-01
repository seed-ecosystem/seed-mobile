package com.seed.api

import com.seed.api.models.EventContent
import com.seed.api.models.ForwardingResponseStatus
import com.seed.api.models.IncomingContent
import com.seed.api.models.SendMessageRequest
import com.seed.api.models.SubscribeRequest
import com.seed.api.util.SeedSocket
import com.seed.domain.EngineEvent
import com.seed.domain.Logger
import com.seed.domain.ResponseQueueItem
import com.seed.domain.SeedEngine
import com.seed.domain.SocketSendResult
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedApi
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.model.ApiEvent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun SeedApi(
	logger: Logger,
	engine: SeedEngine,
): SeedApi {
	return object : SeedApi {
		private val _apiEvents = MutableSharedFlow<ApiEvent>()
		override val apiEvents: SharedFlow<ApiEvent> = _apiEvents

		override val connectionState: StateFlow<SocketConnectionState> = engine.connectionState

		override fun launchConnection(coroutineScope: CoroutineScope) {
			coroutineScope.launch {
				engine.events.collect { socketEvent ->
					when (socketEvent) {
						is EngineEvent.IncomingContent -> {
							val incomingMessage = parseSocketEvent(socketEvent)

							if (incomingMessage is IncomingContent.IncomingResponse) {
								if (engine.responseQueue.size > 0) {
									engine.responseQueue[0](ResponseQueueItem(incomingMessage.response.status))
									engine.responseQueue.removeAt(0)
								}
							}

							if (incomingMessage is IncomingContent.SubscribeEvent) {
								_apiEvents.emit(
									incomingMessage.toChatEvent()
								)
							}
						}

						is EngineEvent.Reconnection -> {
							_apiEvents.emit(ApiEvent.Reconnection)
						}

						EngineEvent.Connected -> _apiEvents.emit(ApiEvent.Connected)

						EngineEvent.Disconnected -> _apiEvents.emit(ApiEvent.Disconnected)
					}
				}
			}
		}

		override suspend fun stopConnection() {
			engine.stop()
		}

		private fun parseSocketEvent(incomingContent: EngineEvent.IncomingContent): IncomingContent? {
			return try {
				Json.decodeFromString<IncomingContent>(incomingContent.content)
			} catch (ex: SerializationException) {
				try {
					val forwardingResponse =
						Json.decodeFromString<ForwardingResponseStatus>(incomingContent.content)

					logger.d(
						tag = "SeedApi",
						message = "Forwarding request status (${incomingContent.url.value}): ${forwardingResponse.status}"
					)

					null
				} catch (ex: SerializationException) {

					logger.e("SeedApi", "Parsing error: ${ex.message}")
					null
				}
			}
		}

		override suspend fun sendMessage(
			chatId: ChatId,
			serverUrl: ServerUrl,
			content: String,
			contentIv: String,
			nonce: ServerNonce,
			signature: String
		): ApiResponse<Unit> {
			val jsonRequest = Json.encodeToString(
				SendMessageRequest.createSendMessageRequest(
					chatId = chatId.value,
					content = content,
					contentIv = contentIv,
					nonce = nonce.value,
					signature = signature,
				)
			)

			val sendResult = engine.send(serverUrl, jsonRequest)

			if (sendResult == SocketSendResult.FAILURE) {
				return ApiResponse.Failure()
			}

			logger.d(
				tag = "SeedApi",
				message = "Sent json: $jsonRequest"
			)

			return suspendCoroutine { continuation ->
				engine.responseQueue.add { response: ResponseQueueItem ->
					logger.d(
						tag = "SeedApi",
						message = "sendMessage: Response: $response",
					)

					if (response.status) continuation.resume(ApiResponse.Success(Unit))
					else continuation.resume(ApiResponse.Failure())
				}
			}
		}

		override suspend fun subscribeToChat(
			chatId: ChatId,
			nonce: ServerNonce,
			serverUrl: ServerUrl,
		): ApiResponse<Unit> {
			val subscribeRequest = SubscribeRequest(
				type = "subscribe",
				queueId = chatId.value,
				nonce = nonce.value,
			)
			val jsonRequest = Json.encodeToString(subscribeRequest)

			val sendResult = engine.send(
				serverUrl = serverUrl,
				jsonRequest = jsonRequest,
			)

			if (sendResult == SocketSendResult.FAILURE) {
				return ApiResponse.Failure()
			}

			logger.d(
				tag = "SeedApi",
				message = "Sent $jsonRequest"
			)

			return suspendCoroutine { continuation ->
				engine.responseQueue.add { response: ResponseQueueItem ->
					logger.d(
						tag = "SeedApi",
						message = "Subscribe response: $response"
					)

					if (response.status) continuation.resume(ApiResponse.Success(Unit))
					else continuation.resume(ApiResponse.Failure())
				}
			}
		}
	}
}

private fun IncomingContent.SubscribeEvent.toChatEvent(): ApiEvent {
	return when (this.event) {
		is EventContent.New -> {
			val newMessage = this.event.message

			ApiEvent.New(
				chatId = ChatId(newMessage.queueId),
				encryptedContentBase64 = newMessage.content,
				encryptedContentIv = newMessage.contentIV,
				nonce = ServerNonce(newMessage.nonce),
				signature = newMessage.signature
			)
		}

		is EventContent.Disconnected -> {
			ApiEvent.ServerDisconnect(
				url = ServerUrl(this.event.url)
			)
		}

		is EventContent.Wait -> {
			ApiEvent.Wait(ChatId(this.event.queueId))
		}
	}
}
