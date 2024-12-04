package com.seed.domain.usecase

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMessageKeyUseCase(
	private val coder: SeedCoder,
	private val chatRepository: ChatRepository
) {
	suspend operator fun invoke(
		chatId: String,
		nonce: Int,
	): String? = withContext(Dispatchers.Default) {
		val cachedKey = chatRepository.getChatKey(chatId, nonce)

		if (cachedKey != null) return@withContext cachedKey

		val getLastChatKeyResult = chatRepository.getLastChatKey(chatId = chatId)
			?: return@withContext null

		if (getLastChatKeyResult.keyNonce <= nonce) {
			val key = deriveTillNonce(
				key = getLastChatKeyResult.key,
				keyNonce = getLastChatKeyResult.keyNonce,
				nonce = nonce,
			)

			chatRepository.insertChatKey(chatId, nonce, key)

			return@withContext key
		} else {
			val oldestChatKey = chatRepository.getOldestChatKey(chatId) ?: return@withContext null

			if (oldestChatKey.keyNonce > nonce) return@withContext null

			val key = deriveTillNonce(
				key = oldestChatKey.key,
				keyNonce = oldestChatKey.keyNonce,
				nonce = nonce,
			)

			return@withContext key
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