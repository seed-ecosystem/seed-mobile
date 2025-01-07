package com.seed.domain

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatKeyRepository

interface KeyManager {
	/**
	 * Generates and stores keys sequentially from last persisted till final nonce
	 */
	suspend fun deriveKeysTillNonce(
		chatId: String,
		tillNonce: Int
	)

	/**
	 * Retrieves key from buffer. If absent, checks persistence. If still absent, derives and persists new key.
	 */
	suspend fun getKey(
		chatId: String,
		nonce: Int,
	): String?

	/**
	 * Clears buffer for specific chat id
	 */
	fun clearBuffer(chatId: String)

	/**
	 * Clears entire buffer
	 */
	fun clearBuffer()
}

fun KeyManager(
	coder: SeedCoder,
	chatKeyRepository: ChatKeyRepository,
	logger: Logger,
): KeyManager {
	return object : KeyManager {
		// TODO: use value objects
		private val derivedKeyCache = mutableMapOf<Pair<String, Int>, String>()

		override suspend fun deriveKeysTillNonce(
			chatId: String,
			tillNonce: Int
		) {
			val startTime = System.nanoTime()

			val lastKey = chatKeyRepository.getLastChatKey(chatId) ?: return

			if (tillNonce <= lastKey.keyNonce) return

			var previousKey = lastKey.key

			for (nonce in lastKey.keyNonce..tillNonce) {
				val result = coder.deriveNextKey(previousKey)

				previousKey = result

				derivedKeyCache[chatId to nonce] = result
			}

			chatKeyRepository.insertKeys(
				chatId = chatId,
				keys = derivedKeyCache
					.filter { (chatIdWithNonce, _) ->
						chatIdWithNonce.first == chatId
					}
					.map { (chatIdWithNonce, key) ->
						Pair(key, chatIdWithNonce.second)
					}
			)

			val finalTime = System.nanoTime()

			logger.d(
				tag = "KeyManager",
				message = "Done nonce calculations from ${lastKey.keyNonce} till $tillNonce by ${finalTime - startTime}ns"
			)
		}

		override suspend fun getKey(chatId: String, nonce: Int): String? {
			val cachedKey = derivedKeyCache[chatId to nonce]
				?: chatKeyRepository.getChatKey(chatId, nonce)

			if (cachedKey != null) return cachedKey

			val lastChatKey = chatKeyRepository.getLastChatKey(chatId = chatId) ?: return null

			val key = if (lastChatKey.keyNonce <= nonce) {
				deriveTillNonce(
					key = lastChatKey.key,
					keyNonce = lastChatKey.keyNonce,
					nonce = nonce
				)
			} else {
				val oldestChatKey = chatKeyRepository.getOldestChatKey(chatId) ?: return null
				if (oldestChatKey.keyNonce > nonce) return null

				deriveTillNonce(
					key = oldestChatKey.key,
					keyNonce = oldestChatKey.keyNonce,
					nonce = nonce
				)
			}

			chatKeyRepository.insertChatKey(chatId, nonce, key)
			derivedKeyCache[chatId to nonce] = key

			return key
		}

		override fun clearBuffer(chatId: String) {
			derivedKeyCache
				.filter {
					it.key.first == chatId
				}
				.forEach { (key, _) ->
					derivedKeyCache.remove(key)
				}
		}

		override fun clearBuffer() {
			derivedKeyCache.clear()
		}

		private suspend fun deriveTillNonce(
			key: String,
			keyNonce: Int,
			nonce: Int,
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
}
