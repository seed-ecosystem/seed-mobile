package com.seed.api.util

import com.seed.domain.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.EOFException
import java.net.SocketException

data class SocketEvent(
	val content: String,
)

interface SeedSocket {
	val socketConnectionEvents: SharedFlow<SocketEvent>

	suspend fun send(jsonContent: String)
	fun launchSocketConnection(coroutineScope: CoroutineScope)
}

fun createSeedSocket(
	logger: Logger,
	host: String,
	path: String,
	reconnectionIntervalMillis: Long,
) = object : SeedSocket {
	private val client = HttpClient(OkHttp) {
		install(WebSockets)
	}

	val websocketSession = CompletableDeferred<DefaultClientWebSocketSession>()

	private val _socketConnectionEvents = MutableSharedFlow<SocketEvent>()
	override val socketConnectionEvents = _socketConnectionEvents

	override fun launchSocketConnection(coroutineScope: CoroutineScope) {
		logger.d(
			tag = "SeedSocket",
			message = "launchSocketConnection: Launching socket connection…"
		)

		coroutineScope.launch {
			while (true) {
				try {
					client.wss(
						method = HttpMethod.Get,
						host = host,
						path = path,
					) {
						websocketSession.complete(this)

						logger.d(
							tag = "SeedSocket",
							message = "launchSocketConnection: Launched socket connection"
						)

						while (true) {
							val received = incoming.receive()

							(received as? Frame.Text)?.readText()?.let { receivedText ->
								_socketConnectionEvents.emit(
									SocketEvent(
										content = receivedText
									)
								)
							}
						}
					}
				} catch (ex: Exception) {
					if (ex is EOFException || ex is SocketException) {
						logger.e(
							tag = "SeedSocket",
							message = "Socket reconnection in ${reconnectionIntervalMillis}ms",
						)

						delay(reconnectionIntervalMillis)
					}
				}
			}
		}
	}

	override suspend fun send(jsonContent: String) {
		val session = websocketSession.await()
		session.send(Frame.Text(jsonContent))
		logger.d(tag = "SeedSocket", message = "send: Sent JSON: $jsonContent")
	}
}