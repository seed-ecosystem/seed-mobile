package com.seed.domain.usecase

import com.seed.domain.api.SeedEngine
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.data.ChatsRepository
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl

class AddChatUseCase(
	private val engine: SeedEngine,
	private val chatRepository: ChatsRepository,
	private val workerStateHandle: SeedWorkerStateHandle,
) {
	suspend operator fun invoke(
		key: ChatKey,
		keyNonce: ServerNonce,
		name: String,
		chatId: ChatId,
		serverUrl: ServerUrl,
	) {
		engine.connectServer(serverUrl)

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
			serverUrl = serverUrl,
		)
	}
}
