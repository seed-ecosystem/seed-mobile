package com.seed.domain

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

interface KeyManager {
	/**
	 * Generates and stores keys sequentially from last persisted till final nonce
	 */
	suspend fun deriveKeysTillNonce(
		chatId: ChatId,
		tillNonce: ServerNonce,
	)

	/**
	 * Retrieves key from buffer. If absent, checks persistence. If still absent, derives and persists new key.
	 */
	suspend fun getKey(
		chatId: ChatId,
		nonce: ServerNonce,
	): ChatKey?

	/**
	 * Clears buffer for specific chat id
	 */
	fun clearBuffer(chatId: ChatId)

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
		private val derivedKeyCache = hashMapOf<Pair<ChatId, ServerNonce>, ChatKey>()

		override suspend fun deriveKeysTillNonce(
			chatId: ChatId,
			tillNonce: ServerNonce
		) {
			val lastKey = chatKeyRepository.getLastChatKey(chatId) ?: return
			val startKeyNonce: ServerNonce = lastKey.keyNonce + 1

			if (tillNonce <= lastKey.keyNonce) return

			withContext(Dispatchers.Default) {
				var previousKey = lastKey.key

				for (nonce in startKeyNonce..tillNonce) {
					val result = coder.deriveNextKey(previousKey)

					previousKey = result

					derivedKeyCache[chatId to ServerNonce(nonce)] = result
				}
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
		}

		override suspend fun getKey(chatId: ChatId, nonce: ServerNonce): ChatKey? {
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

		override fun clearBuffer(chatId: ChatId) {
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
}
