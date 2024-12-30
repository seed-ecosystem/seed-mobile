package com.seed.domain

import com.seed.domain.data.ChatRepository

suspend fun saveNewMessages(
	chatRepository: ChatRepository,
	worker: SeedWorker,
) {
	worker.events.collect { event ->
		if (event is WorkerEvent.New) {
			chatRepository.addMessage(
				chatId = event.chatId,
				message = event.message,
			)
		}
	}
}
