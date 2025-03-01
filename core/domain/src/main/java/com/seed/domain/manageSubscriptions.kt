package com.seed.domain

import com.seed.domain.api.ApiResponse
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun manageSubscriptions(
	chatsRepository: ChatsRepository,
	chatRepository: ChatRepository,
	worker: SeedWorker,
	scope: CoroutineScope,
	logger: Logger,
) {
	scope.launch {
		worker.events.collect { event ->
			if (event !is WorkerEvent.Connected) return@collect
			scope.launch {
				delay(1500)
				subscribeToEachChat(chatsRepository, chatRepository, worker, logger)
			}
		}
	}
}

private suspend fun subscribeToEachChat(
	chatsRepository: ChatsRepository,
	chatRepository: ChatRepository,
	worker: SeedWorker,
	logger: Logger,
) {
	chatsRepository.getAllChatsList().forEach { chat ->
		val lastChatNonce = chatRepository
			.getMessages(chat.chatId)
			.maxByOrNull { it.nonce.value }
			?.nonce ?: chat.firstChatKeyNonce

		val subscriptionResult = worker.subscribe(
			chat.chatId,
			lastChatNonce,
			chat.serverUrl,
		)

		if (subscriptionResult is ApiResponse.Failure) {
			logger.e(
				tag = "manageSubscriptions",
				message = "Subscription error on chat $chat",
			)
		}
	}
}
