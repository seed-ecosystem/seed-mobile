package com.seed.domain.usecase

import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.data.ChatsRepository

class AddChatUseCase(
	private val chatRepository: ChatsRepository,
	private val workerStateHandle: SeedWorkerStateHandle,
) {
	suspend operator fun invoke(
		key: String,
		keyNonce: Int,
		name: String,
		chatId: String,
	) {
		chatRepository.add(
			key = key,
			keyNonce = keyNonce,
			name = name,
			chatId = chatId,
		)

		workerStateHandle.subscribe(
			chatId = chatId,
			nonce = keyNonce,
		)
	}
}
