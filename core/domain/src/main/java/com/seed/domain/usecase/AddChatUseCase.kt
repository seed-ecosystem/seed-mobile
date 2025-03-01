package com.seed.domain.usecase

import com.seed.domain.SeedEngine
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.data.ChatsRepository
import com.seed.domain.values.ServerUrl

class AddChatUseCase(
	private val engine: SeedEngine,
	private val chatRepository: ChatsRepository,
	private val workerStateHandle: SeedWorkerStateHandle,
) {
	suspend operator fun invoke(
		key: String,
		keyNonce: Int,
		name: String,
		chatId: String,
		serverUrl: String,
	) {
		engine.connectServer(ServerUrl(serverUrl))

		chatRepository.add(
			key = key,
			keyNonce = keyNonce,
			name = name,
			chatId = chatId,
			serverUrl = serverUrl,
		)

		workerStateHandle.subscribe(
			chatId = chatId,
			nonce = keyNonce,
			serverUrl = ServerUrl(serverUrl),
		)
	}
}
