package com.seed.data

import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.data.GetLastChatKeyResult
import com.seed.domain.data.GetOldestChatKeyResult
import com.seed.persistence.dao.ChatKeyDao
import com.seed.persistence.dbo.ChatKeyDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatKeyRepositoryImpl(
	private val chatKeyDao: ChatKeyDao,
) : ChatKeyRepository {
	override suspend fun insertChatKey(chatId: String, nonce: Int, key: String) =
		withContext(Dispatchers.IO) {
			chatKeyDao.set(
				ChatKeyDbo(
					key = key,
					chatId = chatId,
					nonce = nonce,
				)
			)
		}

	override suspend fun getChatKey(chatId: String, nonce: Int): String? =
		withContext(Dispatchers.IO) {
			val chatKey = chatKeyDao.getByNonce(chatId, nonce)

			return@withContext chatKey?.key
		}

	override suspend fun getLastChatKey(chatId: String): GetLastChatKeyResult? =
		withContext(Dispatchers.IO) {
			val chatKeyDbo = chatKeyDao.getLatest(chatId) ?: return@withContext null

			return@withContext GetLastChatKeyResult(
				key = chatKeyDbo.key,
				keyNonce = chatKeyDbo.nonce
			)
		}

	override suspend fun getOldestChatKey(chatId: String): GetOldestChatKeyResult? =
		withContext(Dispatchers.IO) {
			val chatKeyDbo = chatKeyDao.getOldest(chatId) ?: return@withContext null

			return@withContext GetOldestChatKeyResult(
				key = chatKeyDbo.key,
				keyNonce = chatKeyDbo.nonce
			)
		}
}