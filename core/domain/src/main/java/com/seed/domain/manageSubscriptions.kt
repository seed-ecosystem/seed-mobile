package com.seed.domain

import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun manageSubscriptions(
	chatsRepository: ChatsRepository,
	chatRepository: ChatRepository,
	worker: SeedWorker,
	scope: CoroutineScope,
) {
	scope.launch {
		worker.events.collect { event ->
			if (event !is WorkerEvent.Connected) return@collect

			subscribeToEachChat(chatsRepository, chatRepository, worker)
		}
	}
}

private suspend fun subscribeToEachChat(
	chatsRepository: ChatsRepository,
	chatRepository: ChatRepository,
	worker: SeedWorker
) {
	chatsRepository.getAllChatsList().forEach { chat ->
		val lastChatNonce = chatRepository
			.getMessages(chat.chatId)
			.maxByOrNull { it.nonce }
			?.nonce ?: chat.firstChatKeyNonce

		println("lastChatNonce $lastChatNonce")

		val subcriptionResult = worker.subscribe(chat.chatId, lastChatNonce) // TODO
	}
}
