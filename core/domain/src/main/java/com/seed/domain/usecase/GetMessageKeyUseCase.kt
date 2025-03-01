package com.seed.domain.usecase

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce

class GetMessageKeyUseCase(
	private val coder: SeedCoder,
	private val chatKeyRepository: ChatKeyRepository
) {
	suspend operator fun invoke(
		chatId: ChatId,
		nonce: ServerNonce,
	): ChatKey? {
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

	private fun deriveTillNonce(
		key: ChatKey,
		keyNonce: ServerNonce,
		nonce: ServerNonce,
	): ChatKey {
		var tempKey = key
		var tempKeyNonce = keyNonce

		while (tempKeyNonce != nonce) {
			tempKey = coder.deriveNextKey(tempKey)
			tempKeyNonce += 1
		}

		return tempKey
	}
}
