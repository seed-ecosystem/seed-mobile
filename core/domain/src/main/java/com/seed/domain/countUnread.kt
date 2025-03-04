package com.seed.domain

import com.seed.domain.data.ChatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun countUnread(
	workerStateHandle: SeedWorkerStateHandle,
	currentChatHandler: CurrentChatHandler,
	chatsRepository: ChatsRepository,
	coroutineScope: CoroutineScope,
) {
	coroutineScope.launch {
		workerStateHandle.events.collect { event ->
			if (event !is WorkerStateHandleEvent.New) return@collect
			if (currentChatHandler.currentChatState.value == event.chatId) return@collect

			println(currentChatHandler.currentChatState.value == event.chatId)
			println("${currentChatHandler.currentChatState.value} == ${event.chatId}")

			val chatId = event.chatId
			val count = event.messages.size

			chatsRepository.addUnreadCount(count, chatId)
		}
	}
}
