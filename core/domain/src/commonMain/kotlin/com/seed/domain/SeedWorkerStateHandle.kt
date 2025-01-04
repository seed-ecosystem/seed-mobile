package com.seed.domain

import com.seed.domain.api.SocketConnectionState
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface WorkerStateHandleEvent {
	data class New(
		val chatId: String,
		val messages: List<MessageContent.RegularMessage>,
	) : WorkerStateHandleEvent

	data class Wait(val chatId: String) : WorkerStateHandleEvent

	data class Unknown(
		val nonce: Int
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
		chatId: String,
		messageContent: MessageContent.RegularMessage,
	)

	suspend fun subscribe(
		chatId: String,
		nonce: Int,
	)

	suspend fun isWaiting(chatId: String): Boolean
}

fun SeedWorkerStateHandle(
	worker: SeedWorker,
	getScope: GetApplicationCoroutineScope,
	logger: Logger,
): SeedWorkerStateHandle {
	val events = MutableSharedFlow<WorkerStateHandleEvent>()
	val waitingChatIds = mutableListOf<String>()

	val accumulatedMessageDefers = mutableMapOf<String, MutableList<Deferred<MessageContent>>>()

	return object : SeedWorkerStateHandle {
		override val connectionState: StateFlow<SocketConnectionState> = worker.connectionState
		override val events: SharedFlow<WorkerStateHandleEvent> = events

		override fun initializeWorkerStateHandle() {
			logger.d(
				tag = "SeedWorkerStateHandle",
				message = "initializeWorkerStateHandle"
			)

			getScope().launch {
				worker.events.collect { event ->
					when (event) {
						is WorkerEvent.Connected -> events.emit(WorkerStateHandleEvent.Connected)
						is WorkerEvent.Disconnected -> events.emit(WorkerStateHandleEvent.Disconnected)
						is WorkerEvent.Reconnection -> events.emit(WorkerStateHandleEvent.Reconnection)

						is WorkerEvent.DeferredNewEvent -> {
							if (!waitingChatIds.contains(event.chatId)) {
								logger.d(
									tag = "SeedWorkerStateHandle",
									message = "Adding deferred chat event"
								)

								val deferList = accumulatedMessageDefers.getOrPut(event.chatId) {
									mutableListOf()
								}
								deferList.add(event.deferredEvent)
							} else {
								logger.d(
									tag = "SeedWorkerStateHandle",
									message = "Start emitting after Wait"
								)

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
							logger.d(
								tag = "SeedWorkerStateHandle",
								message = ""
							)

							val awaitedMessages =
								accumulatedMessageDefers[event.chatId]
									?.toList()
									?.awaitAll()
									?.filterIsInstance<MessageContent.RegularMessage>()
									?.sortedBy { it.nonce }

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
			chatId: String,
			messageContent: MessageContent.RegularMessage
		) = worker.sendMessage(chatId, messageContent)

		override suspend fun subscribe(chatId: String, nonce: Int) =
			worker.subscribe(chatId, nonce)

		override suspend fun isWaiting(chatId: String): Boolean =
			waitingChatIds.contains(chatId)
	}
}
