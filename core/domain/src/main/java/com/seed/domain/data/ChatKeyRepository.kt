package com.seed.domain.data

interface ChatKeyRepository {
	suspend fun insertChatKey(chatId: String, nonce: Int, key: String)

	suspend fun getChatKey(chatId: String, nonce: Int): String?

	suspend fun getLastChatKey(chatId: String): GetLastChatKeyResult?

	suspend fun getOldestChatKey(chatId: String): GetOldestChatKeyResult?
}