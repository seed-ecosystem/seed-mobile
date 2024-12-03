package com.seed.domain.usecase

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository

class GetMessageKeyUseCase(
	private val coder: SeedCoder,
	private val chatRepository: ChatRepository
) {
	suspend operator fun invoke(
		chatId: String,
		nonce: Int,
	): String? {
		val cachedKey = chatRepository.getChatKey(chatId, nonce)

		if (cachedKey != null) return cachedKey

		val getLastChatKeyResult = chatRepository.getLastChatKey(chatId = chatId) ?: return null

		var key = getLastChatKeyResult.key
		var keyNonce = getLastChatKeyResult.keyNonce

		while (keyNonce != nonce) {
			key = coder.deriveNextKey(key)
			keyNonce++
		}

		chatRepository.insertChatKey(chatId, nonce, key)

		return key
	}
}