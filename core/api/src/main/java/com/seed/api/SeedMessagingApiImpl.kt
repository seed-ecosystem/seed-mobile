package com.seed.api

import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.GetHistoryResponse
import com.seed.domain.api.SeedMessagingApi
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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal data class Response(
	val responseJson: String
)

@Serializable
data class HistoryRequest(
	val type: String,
	val nonce: Int?,
	val chatId: String,
	val amount: Int,
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

						responseQueue[0](
							Response(
								(received as? Frame.Text)?.readText() ?: "Unknown received"
							)
						)
						responseQueue.removeAt(0)

						logger.d(
							tag = "SeedMessagingApi",
							message = """
								Received: ${(received as? Frame.Text) ?: "Unknown received"}
								${(received as? Frame.Text)?.readText()}
							""".trimIndent()
						)
					}
				}
			}
		}

		override suspend fun getHistory(
			chatId: String,
			amount: Int,
			nonce: Int?,
		): ApiResponse<GetHistoryResponse> {
			websocketSession.await().let {
				val jsonRequest = Json.encodeToString(
					HistoryRequest(
						type = "history",
						nonce = nonce,
						chatId = chatId,
						amount = amount,
					)
				)

				it.send(jsonRequest)

				logger.d(
					tag = "SeedMessagingApi getHistory",
					message = "Sent json: $jsonRequest"
				)
			}

			return suspendCoroutine { continuation ->
				responseQueue.add { response ->
					logger.d(
						tag = "SeedMessagingApi getHistory",
						message = "Get response: ${response.responseJson}"
					)

					val decoded = Json.decodeFromString<GetHistoryResponse>(response.responseJson)

					continuation.resume(
						ApiResponse.Success(
							GetHistoryResponse(
								messages = decoded.messages
							)
						)
					)
				}
			}
		}

		override suspend fun sendMessage(
			chatId: String,
			content: String,
			contentIv: String,
			nonce: Int,
			signature: String
		): ApiResponse<Unit> {
			TODO("Not yet implemented")
		}
	}
}