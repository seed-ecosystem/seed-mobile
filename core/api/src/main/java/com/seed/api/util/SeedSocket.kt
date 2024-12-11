package com.seed.api.util

import com.seed.domain.Logger
import com.seed.domain.api.SocketConnectionState
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

sealed interface SocketEvent {
	data class IncomingContent(
		val content: String,
	) : SocketEvent

	data object Reconnection : SocketEvent

	data object Connected : SocketEvent

	data object Disconnected : SocketEvent
}

interface SeedSocket {
	val socketConnectionEvents: SharedFlow<SocketEvent>
	val connectionState: StateFlow<SocketConnectionState>

	suspend fun send(jsonContent: String)

	fun initializeSocketConnection(coroutineScope: CoroutineScope)

	suspend fun disconnect()
}

fun createSeedSocket(
	logger: Logger,
	host: String,
	path: String,
	reconnectionIntervalMillis: Long,
) = object : SeedSocket {
	private val client = HttpClient(OkHttp) {
		install(WebSockets)
		install(Logging)
	}

	var websocketSession: DefaultClientWebSocketSession? = null

	private val _socketConnectionEvents = MutableSharedFlow<SocketEvent>()
	override val socketConnectionEvents = _socketConnectionEvents

	private val _connectionState = MutableStateFlow(SocketConnectionState.DISCONNECTED)
	override val connectionState: StateFlow<SocketConnectionState> = _connectionState

	var coroutineScope: CoroutineScope? = null
	var reconnectionJob: Job? = null

	override fun initializeSocketConnection(coroutineScope: CoroutineScope) {
		reconnectionJob?.cancel()
		reconnectionJob = null
		websocketSession = null
		this.coroutineScope = coroutineScope

		connect()
	}

	override suspend fun disconnect() {
		stop()
	}

	private fun connect() {
		coroutineScope?.launch {
			try {
				println("yes try block start")

				websocketSession =
					client.webSocketSession(
						block = {
							url.protocol = URLProtocol.WSS
							url.host = host
							url.path(path)
							url.port = url.protocol.defaultPort
						},
					)

				println("yes websocket session initialized correctly :) $websocketSession")

				_socketConnectionEvents.emit(SocketEvent.Connected)
				_connectionState.update { SocketConnectionState.CONNECTED }

				websocketSession!!.incoming
					.receiveAsFlow()
					.filterIsInstance<Frame.Text>()
					.filterNotNull()
					.collect { data ->
						logger.d(tag = "SeedSocket", message = "Received peace of data: ${data.readText()}")

						val message = data.readText()

						_socketConnectionEvents.emit(
							SocketEvent.IncomingContent(
								content = message
							)
						)
					}
			} catch (ex: Exception) {
				_connectionState.update { SocketConnectionState.DISCONNECTED }

				_socketConnectionEvents.emit(SocketEvent.Disconnected)

				logger.e(
					tag = "SeedSocket",
					message = "connect error: ${ex.message}"
				)

				logger.e(
					tag = "SeedSocket",
					message = "Socket reconnection in ${reconnectionIntervalMillis}ms",
				)

				_connectionState.update { SocketConnectionState.RECONNECTING }

				reconnect()

				_socketConnectionEvents.emit(SocketEvent.Reconnection)

			}
		}
	}

	private suspend fun stop() {
		logger.d(tag = "SeedSocket", "stop(): Closing websocket session…")

		websocketSession?.close()
		websocketSession = null

		_connectionState.update { SocketConnectionState.DISCONNECTED }
	}

	private fun reconnect() {
		reconnectionJob?.cancel()

		logger.d(
			tag = "SeedSocket",
			message = "reconnect()"
		)

		reconnectionJob = coroutineScope?.launch {
			stop()
			delay(reconnectionIntervalMillis)
			connect()
		}
	}

	override suspend fun send(jsonContent: String) {
		try {
			while (websocketSession == null) {
				delay(1000)
			}
			websocketSession?.send(Frame.Text(jsonContent))
			if (websocketSession == null) {
				logger.e(
					tag = "SeedSocket",
					message = "Websocket session is null"
				)
			}
			logger.d(tag = "SeedSocket", message = "send: Sent JSON: $jsonContent")
		} catch (ex: Exception) {
			logger.e(
				tag = "SeedSocket",
				message = "An error occured while sending message to the socket: ${ex.message}"
			)
		}
	}
}