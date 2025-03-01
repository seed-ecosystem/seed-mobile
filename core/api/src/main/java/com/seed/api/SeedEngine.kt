package com.seed.api

import com.seed.api.models.IncomingContent
import com.seed.api.util.SeedSocket
import com.seed.api.util.SocketEvent
import com.seed.domain.EngineEvent
import com.seed.domain.ForwardingState
import com.seed.domain.ResponseQueueItem
import com.seed.domain.SeedEngine
import com.seed.domain.SocketSendResult
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.data.ChatsRepository
import com.seed.domain.data.SettingsRepository
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.net.URI

@Serializable
internal data class PingRequest(
	val type: String = "ping",
)

@Serializable
internal data class ConnectForwardingRequest(
	val type: String = "connect",
	val url: String,
)

@Serializable
internal data class ForwardingRequest(
	val type: String = "forward",
	val url: String,
	val request: JsonElement,
)

@Serializable
internal data class ForwardingResponse(
	val type: String = "forward",
	val url: String,
	val forward: JsonElement,
)

fun SeedEngine(
	socket: SeedSocket,
	settingsRepository: SettingsRepository,
	chatsRepository: ChatsRepository,
	defaultMainServerUrl: ServerUrl,
	pingIntervalMillis: Long,
): SeedEngine {
	return object : SeedEngine {
		override val responseQueue: MutableList<(ResponseQueueItem) -> Unit> =
			mutableListOf()

		override val connectionState: StateFlow<SocketConnectionState> = socket.connectionState

		private val _events = MutableSharedFlow<EngineEvent>()
		override val events: SharedFlow<EngineEvent> = _events

		private val _forwardingState = MutableStateFlow(ForwardingState(emptyList()))
		override val forwardingState: StateFlow<ForwardingState> = _forwardingState

		private val json = Json { encodeDefaults = true }

		override fun initialize(scope: CoroutineScope) {
			val mainServerUrl = settingsRepository.getMainServerUrl() ?: defaultMainServerUrl
			val uri = getMainServerUri(mainServerUrl.value)

			scope.launch {
				connectionState.collect { event ->
					if (event == SocketConnectionState.CONNECTED) {
						handleOnConnect(scope)
					}
				}
			}

			scope.launch {
				socket.events.collect { event ->
					handleSocketEvents(event, mainServerUrl)
				}
			}

			socket.initializeSocketConnection(
				host = uri.host,
				path = uri.path,
				coroutineScope = scope,
			)
		}

		private suspend fun handleSocketEvents(event: SocketEvent, mainServerUrl: ServerUrl) {
			when (event) {
				is SocketEvent.IncomingContent -> {
					handleIncomingContent(event, mainServerUrl)
				}

				SocketEvent.Connected -> _events.emit(EngineEvent.Connected)

				SocketEvent.Disconnected -> _events.emit(EngineEvent.Disconnected)

				SocketEvent.Reconnection -> _events.emit(EngineEvent.Reconnection)
			}
		}

		private suspend fun handleIncomingContent(
			event: SocketEvent.IncomingContent,
			mainServerUrl: ServerUrl,
		) {
			try {
				val response: ForwardingResponse = json.decodeFromString(event.content)
				if (response.type == "forward") {
					val innerContent = response.forward.toString()
					_events.emit(
						EngineEvent.IncomingContent(
							url = ServerUrl(response.url),
							content = innerContent,
						)
					)
				}
			} catch (ex: SerializationException) {
				_events.emit(
					EngineEvent.IncomingContent(
						content = event.content,
						url = mainServerUrl,
					)
				)
			}
		}

		private var pingJob: Job? = null

		private suspend fun handleOnConnect(scope: CoroutineScope) {
			val serverUrls = chatsRepository.getAllServerUrls()

			serverUrls.forEach { url: ServerUrl ->
				connectServer(url)
			}

			pingJob?.cancel()

			pingJob = scope.launch {
				sendPingEachMillis()
			}
		}

		private suspend fun sendPingEachMillis() {
			val pingRequestJson = json.encodeToString(PingRequest())

			while (true) {
				val urls = chatsRepository.getAllServerUrls()

				delay(pingIntervalMillis)

				socket.send(pingRequestJson)

				responseQueue.add {}

				urls.forEach { url ->
					send(
						serverUrl = url,
						jsonRequest = pingRequestJson
					)

					responseQueue.add {}
				}
			}
		}

		override suspend fun connectServer(url: ServerUrl) {
			val request = json.encodeToString(
				ConnectForwardingRequest(
					url = url.value.replace(
						"https",
						"wss"
					), // TODO: move this logic to the appropriate place
				)
			)

			socket.send(request) // TODO: add handling for request responses
		}

		override suspend fun stop() {
			socket.disconnect()
		}

		override suspend fun send(serverUrl: ServerUrl, jsonRequest: String): SocketSendResult {
			val forwardingRequest = ForwardingRequest(
				url = serverUrl.value.replace(
					"https",
					"wss"
				), // TODO: move this logic to the appropriate place
				request = json.parseToJsonElement(jsonRequest),
			)
			val forwardingRequestJson = json.encodeToString(forwardingRequest)

			return socket.send(forwardingRequestJson)
		}

		private fun getMainServerUri(mainServerUrl: String): URI {
			val uri = URI.create(mainServerUrl)
			return uri
		}
	}
}
