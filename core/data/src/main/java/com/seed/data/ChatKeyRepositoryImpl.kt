package com.seed.data

import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.data.GetLastChatKeyResult
import com.seed.domain.data.GetOldestChatKeyResult
import com.seed.persistence.db.dao.ChatKeyDao
import com.seed.persistence.db.dbo.ChatKeyDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatKeyRepositoryImpl(
	private val chatKeyDao: ChatKeyDao,
) : ChatKeyRepository {
	override suspend fun insertKeys(chatId: String, keys: List<Pair<String, Int>>) =
		withContext(Dispatchers.IO) {
			val resultingList = keys
				.map {
					ChatKeyDbo(
						chatId = chatId,
						key = it.first,
						nonce = it.second,
					)
				}

			chatKeyDao.insertAll(resultingList)
		}

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

	override suspend fun getChatKey(chatId: String, nonce: Int): String? {
		val chatKey = chatKeyDao.getByNonce(chatId, nonce)

		return chatKey?.key
	}

	override suspend fun getLastChatKey(chatId: String): GetLastChatKeyResult? {
		val chatKeyDbo = chatKeyDao.getLatest(chatId) ?: return null

		return GetLastChatKeyResult(
			key = chatKeyDbo.key,
			keyNonce = chatKeyDbo.nonce
		)
	}

	override suspend fun getOldestChatKey(chatId: String): GetOldestChatKeyResult? {
		val chatKeyDbo = chatKeyDao.getOldest(chatId) ?: return null

		return GetOldestChatKeyResult(
			key = chatKeyDbo.key,
			keyNonce = chatKeyDbo.nonce
		)
	}
}
