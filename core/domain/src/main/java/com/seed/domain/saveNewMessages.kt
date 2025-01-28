package com.seed.domain

import com.seed.domain.data.ChatRepository

suspend fun saveNewMessages(
	chatRepository: ChatRepository,
	worker: SeedWorkerStateHandle,
) {
	worker.events.collect { event ->
		if (event is WorkerStateHandleEvent.New) {
			chatRepository.addMessagesList(
				chatId = event.chatId,
				messages = event.messages
			)
		}
	}
}
