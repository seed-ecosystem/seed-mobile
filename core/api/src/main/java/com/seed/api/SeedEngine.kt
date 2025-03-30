package com.seed.api

import com.seed.api.models.ForwardingResponseStatus
import com.seed.api.models.IncomingContent
import com.seed.api.models.SendMessageRequest
import com.seed.api.models.SubscribeRequest
import com.seed.api.models.toChatEvent
import com.seed.api.util.SeedSocket
import com.seed.api.util.SocketEvent
import com.seed.domain.api.ForwardingState
import com.seed.domain.Logger
import com.seed.domain.api.SeedEngine
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.data.ChatsRepository
import com.seed.domain.data.SettingsRepository
import com.seed.domain.model.ApiEvent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

internal data class ResponseQueueItem(
	val status: Boolean,
)

enum class SocketSendResult {
	SUCCESS,
	FAILURE,
}


fun SeedEngine(
	logger: Logger,
	socket: SeedSocket,
	settingsRepository: SettingsRepository,
	chatsRepository: ChatsRepository,
	defaultMainServerUrl: ServerUrl,
	pingIntervalMillis: Long,
): SeedEngine {
	return object : SeedEngine {
		val responseQueue: MutableList<(ResponseQueueItem) -> Unit> = mutableListOf()

		override val connectionState: StateFlow<SocketConnectionState> = socket.connectionState

		private val _events = MutableSharedFlow<ApiEvent>()
		override val events: SharedFlow<ApiEvent> = _events

		private val _forwardingState = MutableStateFlow(ForwardingState(emptyList()))
		override val forwardingState: StateFlow<ForwardingState> = _forwardingState

		private var pingJob: Job? = null

		private val json = Json { encodeDefaults = true }

		override fun launchConnection(scope: CoroutineScope) {
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
					handleSocketEvents(event)
				}
			}

			socket.initializeSocketConnection(
				host = uri.host,
				path = uri.path,
				coroutineScope = scope,
			)
		}

		private suspend fun handleSocketEvents(event: SocketEvent) {
			when (event) {
				is SocketEvent.IncomingContent -> {
					val unwrapped = unwrapIncomingContent(event.content)

					unwrapped?.let {
						handleUnwrappedIncomingContent(it)
					}
				}

				SocketEvent.Connected -> _events.emit(ApiEvent.Connected)

				SocketEvent.Disconnected -> _events.emit(ApiEvent.Disconnected)

				SocketEvent.Reconnection -> _events.emit(ApiEvent.Reconnection)
			}
		}

		private suspend fun sendToSocket(
			serverUrl: ServerUrl,
			jsonRequest: String
		): SocketSendResult {
			val forwardingRequest = ForwardingRequest(
				url = serverUrl.value,
				request = json.parseToJsonElement(jsonRequest),
			)
			val forwardingRequestJson = json.encodeToString(forwardingRequest)

			return socket.send(forwardingRequestJson)
		}

		private fun unwrapIncomingContent(content: String): String? {
			try {
				val response: ForwardingResponse = json.decodeFromString(content)
				if (response.type == "forward") {
					val innerContent = response.forward.toString()

					return innerContent
				}
			} catch (ex: SerializationException) {
				return content
			}

			return null
		}

		private suspend fun handleUnwrappedIncomingContent(unwrappedContent: String) {
			val incomingMessage = try {
				Json.decodeFromString<IncomingContent>(unwrappedContent)
			} catch (ex: SerializationException) {
				try {
					val forwardingResponse =
						Json.decodeFromString<ForwardingResponseStatus>(unwrappedContent)

					dequeue(forwardingResponse.status)

					null
				} catch (ex: SerializationException) {

					logger.e("SeedApi", "Parsing error: ${ex.message}")
					null
				}
			}

			if (incomingMessage is IncomingContent.IncomingResponse) {
				dequeue(incomingMessage.response.status)
			}

			if (incomingMessage is IncomingContent.SubscribeEvent) {
				_events.emit(
					incomingMessage.toChatEvent()
				)
			}
		}

		private fun dequeue(status: Boolean) {
			if (responseQueue.size > 0) {
				responseQueue[0](ResponseQueueItem(status))
				responseQueue.removeAt(0)
			}
		}

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
			while (true) {
				val urls = chatsRepository.getAllServerUrls()

				delay(pingIntervalMillis)

				sendPing(defaultMainServerUrl)

				urls.forEach { url ->
					sendPing(url)
				}
			}
		}

		override suspend fun stop() {
			socket.disconnect()
		}

		override suspend fun sendPing(
			serverUrl: ServerUrl,
		): ApiResponse<Unit> {
			val pingRequestJson = json.encodeToString(PingRequest())
			sendToSocket(serverUrl, pingRequestJson)

			return suspendCoroutine { continuation ->
				responseQueue.add { response: ResponseQueueItem ->
					if (response.status) continuation.resume(ApiResponse.Success(Unit))
					else continuation.resume(ApiResponse.Failure())
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

			val sendResult = sendToSocket(serverUrl, jsonRequest)

			if (sendResult == SocketSendResult.FAILURE) {
				return ApiResponse.Failure()
			}

			logger.d(
				tag = "SeedApi",
				message = "Sent json: $jsonRequest"
			)

			return suspendCoroutine { continuation ->
				responseQueue.add { response: ResponseQueueItem ->
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
			serverUrl: ServerUrl
		): ApiResponse<Unit> {
			val subscribeRequest = SubscribeRequest(
				type = "subscribe",
				queueId = chatId.value,
				nonce = nonce.value,
			)
			val jsonRequest = Json.encodeToString(subscribeRequest)

			val sendResult = sendToSocket(
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
				responseQueue.add { response: ResponseQueueItem ->
					logger.d(
						tag = "SeedApi",
						message = "Subscribe response: $response"
					)

					if (response.status) continuation.resume(ApiResponse.Success(Unit))
					else continuation.resume(ApiResponse.Failure())
				}
			}
		}

		override suspend fun connectServer(url: ServerUrl) {
			val request = json.encodeToString(
				ConnectForwardingRequest(url = url.value)
			)

			socket.send(request)

			responseQueue.add {
				// TODO()
				// TODO: In case of "If connection was not successful or if it was closed later server will send you the following event:"
				// This one probably won't work as expected
			}
		}

		private fun getMainServerUri(mainServerUrl: String): URI {
			val uri = URI.create(mainServerUrl)
			return uri
		}
	}
}
