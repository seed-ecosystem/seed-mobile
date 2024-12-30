package com.seed.domain

import com.seed.domain.api.SocketConnectionState
import com.seed.domain.model.MessageContent
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
	val accumulatedMessages = mutableMapOf<String, MutableList<WorkerEvent.New>>()

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

						is WorkerEvent.New -> {
							if (waitingChatIds.contains(event.chatId)) {
								logger.d(
									tag = "SeedWorkerStateHandle",
									message = "Emitting after waiting ${event.chatId}"
								)

								events.emit(
									WorkerStateHandleEvent.New(
										chatId = event.chatId,
										messages = listOf(event.message)
									)
								)
							} else {
								logger.d(
									tag = "SeedWorkerStateHandle",
									message = "Accumulating event with nonce ${event.message.nonce}"
								)

								val list =
									accumulatedMessages.getOrPut(event.chatId) { mutableListOf() }
								list.add(event)
							}

// TODO							events.emit(
//								WorkerStateHandleEvent.New(
//									chatId = event.chatId,
//									messages = listOf(event.message)
//								)
//							)
						}

						is WorkerEvent.Wait -> {
							val messages = accumulatedMessages[event.chatId] ?: emptyList()

							logger.d(
								tag = "SeedWorkerStateHandle",
								message = "Emitting list of accumulated messages after wait event for ${event.chatId}"
							)

							events.emit(
								WorkerStateHandleEvent.New(
									chatId = event.chatId,
									messages = messages
										.sortedBy { it.message.nonce }
										.map { it.message }
								)
							)

							accumulatedMessages.remove(event.chatId)
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
