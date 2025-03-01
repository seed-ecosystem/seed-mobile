package com.seed.domain

import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.data.SendMessageDto
import com.seed.domain.data.SendMessageResult
import com.seed.domain.model.MessageContent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface WorkerStateHandleEvent {
	data class New(
		val chatId: ChatId,
		val messages: List<MessageContent.RegularMessage>,
	) : WorkerStateHandleEvent

	data class Wait(val chatId: ChatId) : WorkerStateHandleEvent

	data class Unknown(
		val nonce: ServerNonce,
	) : WorkerStateHandleEvent

	data object Reconnection : WorkerStateHandleEvent

	data object Connected : WorkerStateHandleEvent

	data object Disconnected : WorkerStateHandleEvent
}

interface SeedWorkerStateHandle {
	val connectionState: StateFlow<SocketConnectionState>
	val events: SharedFlow<WorkerStateHandleEvent>

	fun initializeWorkerStateHandle()

	suspend fun sendMessage(
		dto: SendMessageDto,
	): SendMessageResult

	suspend fun subscribe(
		chatId: ChatId,
		nonce: ServerNonce,
		serverUrl: ServerUrl,
	): ApiResponse<Unit>

	suspend fun isWaiting(chatId: ChatId): Boolean
}

fun SeedWorkerStateHandle(
	worker: SeedWorker,
	keyManager: KeyManager,
	getScope: GetApplicationCoroutineScope,
	logger: Logger,
): SeedWorkerStateHandle {
	val events = MutableSharedFlow<WorkerStateHandleEvent>()
	val waitingChatIds = mutableListOf<ChatId>()

	val accumulatedMessageDefers =
		mutableMapOf<ChatId, MutableList<Pair<ServerNonce, Deferred<MessageContent>>>>()

	return object : SeedWorkerStateHandle {
		override val connectionState: StateFlow<SocketConnectionState> = worker.connectionState
		override val events: SharedFlow<WorkerStateHandleEvent> = events

		override fun initializeWorkerStateHandle() {
			getScope().launch {
				worker.events.collect { event ->
					when (event) {
						is WorkerEvent.Connected -> events.emit(WorkerStateHandleEvent.Connected)
						is WorkerEvent.Disconnected -> events.emit(WorkerStateHandleEvent.Disconnected)
						is WorkerEvent.Reconnection -> events.emit(WorkerStateHandleEvent.Reconnection)

						is WorkerEvent.DeferredNewEvent -> {
							val chatIsWaiting = waitingChatIds.contains(event.chatId)
							if (!chatIsWaiting) {
								val deferList = accumulatedMessageDefers.getOrPut(event.chatId) {
									mutableListOf()
								}
								deferList.add(Pair(event.nonce, event.deferredEvent))
							} else {
								val awaited = event.deferredEvent.await()
								if (awaited is MessageContent.RegularMessage) {
									events.emit(
										WorkerStateHandleEvent.New(
											chatId = event.chatId,
											messages = listOf(awaited)
										)
									)
								}
							}
						}

						is WorkerEvent.Wait -> {
							val maxNonce: ServerNonce = accumulatedMessageDefers[event.chatId]
								?.maxBy { it.first.value }
								?.first
								?: ServerNonce(1)

							keyManager.deriveKeysTillNonce(
								chatId = event.chatId,
								tillNonce = maxNonce,
							)

							val awaitedMessages =
								accumulatedMessageDefers[event.chatId]
									?.toList()
									?.map { it.second }
									?.awaitAll()
									?.filterIsInstance<MessageContent.RegularMessage>()
									?.sortedBy { it.nonce.value }

							keyManager.clearBuffer(chatId = event.chatId)

							logger.d(
								tag = "SeedWorkerStateHandle",
								message = "Emitting list of accumulated messages on Wait event for ${event.chatId}\n" +
										"List (${awaitedMessages?.size ?: 0} elements)"
							)

							awaitedMessages?.let {
								events.emit(
									WorkerStateHandleEvent.New(
										chatId = event.chatId,
										messages = awaitedMessages
											.map { it }
									)
								)
							}

							accumulatedMessageDefers.remove(event.chatId)
							waitingChatIds.add(event.chatId)

							events.emit(WorkerStateHandleEvent.Wait(event.chatId))
						}

						is WorkerEvent.Unknown -> events.emit(WorkerStateHandleEvent.Unknown(event.nonce))
					}
				}
			}
		}

		override suspend fun sendMessage(
			dto: SendMessageDto,
		): SendMessageResult = worker.sendMessage(dto)

		override suspend fun subscribe(chatId: ChatId, nonce: ServerNonce, serverUrl: ServerUrl): ApiResponse<Unit> =
			worker.subscribe(chatId, nonce, serverUrl)

		override suspend fun isWaiting(chatId: ChatId): Boolean =
			waitingChatIds.contains(chatId)
	}
}
