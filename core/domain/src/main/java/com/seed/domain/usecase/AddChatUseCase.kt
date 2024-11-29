package com.seed.domain.usecase

import com.seed.domain.data.ChatsRepository

class AddChatUseCase(
	private val chatRepository: ChatsRepository,
) {
	suspend operator fun invoke(
		key: String,
		keyNonce: Int,
		name: String,
	) {
		chatRepository.add(
			key = key,
			keyNonce = keyNonce,
			name = name
		)
	}
}