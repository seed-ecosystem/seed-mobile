package com.seed.api

import com.seed.api.models.EventWrapper
import com.seed.api.models.RawChatEvent
import com.seed.api.models.SendMessageRequest
import com.seed.api.models.SubscribeRequest
import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.model.ChatEvent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal data class Response(
	val responseJson: String
)

fun createSeedMessagingApi(logger: Logger, host: String, path: String): SeedMessagingApi {
	val client = HttpClient(OkHttp) {
		install(WebSockets)
	}

	val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
		logger.e(
			tag = "SeedMessagingApi",
			message = throwable.message.toString()
		)
	}

	val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineExceptionHandler)

	val responseQueue: MutableList<(Response) -> Unit> = mutableListOf()

	val websocketSession = CompletableDeferred<DefaultClientWebSocketSession>()

	return object : SeedMessagingApi {
		init {
			coroutineScope.launch {
				client.wss(
					method = HttpMethod.Get,
					host = host,
					path = path,
				) {
					logger.d(
						tag = "SeedMessagingApi",
						message = "init: Created wss connection"
					)

					websocketSession.complete(this)

					while (true) {
						val received = incoming.receive()

						if (responseQueue.size > 0) {
							responseQueue[0]( // TODO: here should be some check if it is 'response'
								Response(
									(received as? Frame.Text)?.readText() ?: "Unknown received"
								)
							)
							responseQueue.removeAt(0)
						}

						logger.d(
							tag = "SeedMessagingApi",
							message = """
								init: Received: ${(received as? Frame.Text)?.readText() ?: "Unknown received"}
								${(received as? Frame.Text)?.readText()}
							""".trimIndent()
						)

						try {
							val decodedEvent = Json.decodeFromString<EventWrapper>(
								(received as? Frame.Text)?.readText() ?: ""
							)

							_chatEvents.emit(
								rawChatEventToChatEvent(decodedEvent.event)
							)
						} catch (ex: Exception) {
							logger.e(
								tag = "SeedMessagingApi",
								message = "init: Parsing error: ${ex.message}"
							)
						}
					}
				}
			}
		}

		private fun rawChatEventToChatEvent(
			event: RawChatEvent,
		) = when (event) {
			is RawChatEvent.New -> {
				ChatEvent.New(
					messageId = event.message.chatId,
					encryptedContentBase64 = event.message.content,
					encryptedContentIv = event.message.contentIV,
					nonce = event.message.nonce,
					signature = event.message.signature,
				)
			}

			is RawChatEvent.WaitEvent -> {
				ChatEvent.Wait
			}
		}

		private val _chatEvents = MutableSharedFlow<ChatEvent>()
		override val chatEvents: SharedFlow<ChatEvent> = _chatEvents

		override suspend fun sendMessage(
			chatId: String,
			content: String,
			contentIv: String,
			nonce: Int,
			signature: String
		): ApiResponse<Unit> = withContext(Dispatchers.IO) {
			websocketSession.await().let { session ->
				val jsonRequest = Json.encodeToString(
					SendMessageRequest(
						type = "send",
						message = SendMessageRequest.Message(
							chatId = chatId,
							content = content,
							contentIv = contentIv,
							nonce = nonce,
							signature = signature
						)
					)
				)

				session.send(jsonRequest)

				logger.d(
					tag = "SeedMessagingApi",
					message = "getHistory: Sent json: $jsonRequest"
				)
			}

			return@withContext suspendCoroutine { continuation ->
				responseQueue.add { response ->
					logger.d(
						tag = "SeedMessagingApi",
						message = "sendMessage: Response: ${response.responseJson}",
					)

					continuation.resume(ApiResponse.Success(Unit))
				}
			}
		}

		override suspend fun subscribeToChat(chatId: String, nonce: Int): ApiResponse<Unit> =
			withContext(Dispatchers.IO) {
				val subscribeRequest = SubscribeRequest(
					type = "subscribe",
					chatId = chatId,
					nonce = nonce
				)
				val jsonRequest = Json.encodeToString(subscribeRequest)

				val session = websocketSession.await()

				session.send(jsonRequest)

				logger.d(
					tag = "SeedMessagingApi",
					message = "subscribeToChat: Sent $jsonRequest"
				)

				return@withContext suspendCoroutine { continuation ->
					responseQueue.add { response ->
						logger.d(
							tag = "SeedMessagingApi",
							message = "subscribeToChat: Got response: ${response.responseJson}"
						)

						continuation.resume(ApiResponse.Success(Unit))
					}
				}
			}
	}
}