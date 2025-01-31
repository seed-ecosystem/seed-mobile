package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.WorkerStateHandleEvent
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow

sealed interface SubscribeToChatUseCaseEvent {
	data class Stored(
		val chatId: String,
		val messages: List<MessageContent>,
	) : SubscribeToChatUseCaseEvent

	data class New(
		val chatId: String,
		val messages: List<MessageContent.RegularMessage>,
	) : SubscribeToChatUseCaseEvent

	data class Wait(val chatId: String) : SubscribeToChatUseCaseEvent

	data class Unknown(
		val nonce: Int
	) : SubscribeToChatUseCaseEvent

	data object Reconnection : SubscribeToChatUseCaseEvent

	data object Connected : SubscribeToChatUseCaseEvent

	data object Disconnected : SubscribeToChatUseCaseEvent
}

class SubscribeToChatUseCase(
	private val chatRepository: ChatRepository,
	private val workerStateHandle: SeedWorkerStateHandle,
	private val logger: Logger,
) {
	suspend operator fun invoke(
		chatId: String
	): Flow<SubscribeToChatUseCaseEvent> {
		return flow {
			val messages = chatRepository.getMessages(chatId)
				.sortedBy { it.nonce }

			logger.d(
				tag = "SubscribeToChatUseCase",
				message = "Emitting stored message list for $chatId"
			)

			emit(
				SubscribeToChatUseCaseEvent.Stored(
					chatId = chatId,
					messages = messages,
				)
			)

			workerStateHandle
				.events
				.filter { event ->
					return@filter when (event) {
						is WorkerStateHandleEvent.New -> {
							event.chatId == chatId
						}

						is WorkerStateHandleEvent.Wait -> {
							event.chatId == chatId
						}

						is WorkerStateHandleEvent.Unknown -> false

						else -> true
					}
				}
				.collect { event ->
					when (event) {
						is WorkerStateHandleEvent.New -> emit(
							SubscribeToChatUseCaseEvent.New(
								event.chatId,
								event.messages
							)
						)

						WorkerStateHandleEvent.Connected -> emit(SubscribeToChatUseCaseEvent.Connected)
						WorkerStateHandleEvent.Disconnected -> emit(SubscribeToChatUseCaseEvent.Disconnected)
						WorkerStateHandleEvent.Reconnection -> emit(SubscribeToChatUseCaseEvent.Reconnection)
						is WorkerStateHandleEvent.Unknown -> emit(SubscribeToChatUseCaseEvent.Unknown(event.nonce))
						is WorkerStateHandleEvent.Wait -> emit(SubscribeToChatUseCaseEvent.Wait(event.chatId))
					}
				}
		}
	}
}
