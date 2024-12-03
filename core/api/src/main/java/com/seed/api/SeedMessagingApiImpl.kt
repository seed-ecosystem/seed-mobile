package com.seed.api

import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.model.ChatEvent
import com.seed.domain.usecase.DecodedChatEvent
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal data class Response(
	val responseJson: String
)

@Serializable
data class SubscribeRequest(
	val type: String,
	val nonce: Int,
	val chatId: String,
)

@Serializable
data class SendMessageRequest(
	val type: String,
	val message: Message
) {
	@Serializable
	data class Message(
		val chatId: String,
		val content: String,
		val contentIv: String,
		val nonce: Int,
		val signature: String,
	)
}

@Serializable
internal sealed interface RawChatEvent {
	val type: String

	@Serializable
	@SerialName("new")
	data class New(
		override val type: String,
		val message: Message
	) : RawChatEvent {
		@Serializable
		data class Message(
			val nonce: Int,
			val chatId: String,
			val signature: String,
			val content: String,
			val contentIV: String
		)
	}

	@Serializable
	@SerialName("wait")
	data class WaitEvent(
		val chatId: String
	) : RawChatEvent {
		override val type: String = "wait"
	}
}

@Serializable
internal data class EventWrapper(
	val type: String,
	val event: RawChatEvent,
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
						message = "Created wss connection"
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
								Received: ${(received as? Frame.Text)?.readText() ?: "Unknown received"}
								${(received as? Frame.Text)?.readText()}
							""".trimIndent()
						)

						try {
							val decodedEvent = Json.decodeFromString<EventWrapper>(
								(received as? Frame.Text)?.readText() ?: ""
							)
							logger.d(
								tag = "SeedMessagingApi",
								message = "PARSED: $decodedEvent"
							)

							_chatEvents.emit(
								rawChatEventToChatEvent(decodedEvent.event)
							)
						} catch (ex: Exception) {
							logger.e(
								tag = "SeedMessagingApi",
								message = "NOT PARSED: ${ex.message}"
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
					tag = "SeedMessagingApi getHistory",
					message = "Sent json: $jsonRequest"
				)
			}

			return@withContext suspendCoroutine { continuation ->
				responseQueue.add { response ->
					logger.d(
						tag = "SeedMessagingApi sendMessage",
						message = "Response: ${response.responseJson}",
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
					tag = "SeedMessagingApi subscribeToChat",
					message = "Sent $jsonRequest"
				)

				return@withContext suspendCoroutine { continuation ->
					responseQueue.add { response ->
						logger.d(
							tag = "SeedMessagingApi subscribeToChat",
							message = "Response: ${response.responseJson}"
						)

						continuation.resume(ApiResponse.Success(Unit))
					}
				}
			}
	}
}