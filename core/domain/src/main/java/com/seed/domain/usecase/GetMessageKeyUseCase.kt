package com.seed.domain.usecase

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatKeyRepository

class GetMessageKeyUseCase(
	private val coder: SeedCoder,
	private val chatKeyRepository: ChatKeyRepository
) {
	suspend operator fun invoke(
		chatId: String,
		nonce: Int,
	): String? {
		val cachedKey = chatKeyRepository.getChatKey(chatId, nonce)

		if (cachedKey != null) return cachedKey

		val getLastChatKeyResult = chatKeyRepository.getLastChatKey(chatId = chatId) ?: return null

		if (getLastChatKeyResult.keyNonce <= nonce) {
			val key = deriveTillNonce(
				key = getLastChatKeyResult.key,
				keyNonce = getLastChatKeyResult.keyNonce,
				nonce = nonce,
			)

			chatKeyRepository.insertChatKey(chatId, nonce, key)

			return key
		} else {
			val oldestChatKey = chatKeyRepository.getOldestChatKey(chatId) ?: return null

			if (oldestChatKey.keyNonce > nonce) return null

			val key = deriveTillNonce(
				key = oldestChatKey.key,
				keyNonce = oldestChatKey.keyNonce,
				nonce = nonce,
			)

			return key
		}
	}

	private suspend fun deriveTillNonce(
		key: String,
		keyNonce: Int,
		nonce: Int
	): String {
		var tempKey = key
		var tempKeyNonce = keyNonce

		while (tempKeyNonce != nonce) {
			tempKey = coder.deriveNextKey(tempKey)
			tempKeyNonce++
		}
		return tempKey
	}
}
