package com.seed.domain

import com.seed.domain.data.ChatRepository

suspend fun saveNewMessages(
	chatRepository: ChatRepository,
	worker: SeedWorkerStateHandle,
) {
	worker.events.collect { event ->
		if (event is WorkerStateHandleEvent.New) {
			event.messages.forEach {

				chatRepository.addMessage(
					chatId = event.chatId,
					message = it,
				)
			}
		}
	}
}
