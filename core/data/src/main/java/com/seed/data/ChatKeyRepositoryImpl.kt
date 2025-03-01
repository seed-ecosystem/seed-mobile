package com.seed.data

import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.data.GetLastChatKeyResult
import com.seed.domain.data.GetOldestChatKeyResult
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.persistence.db.dao.ChatKeyDao
import com.seed.persistence.db.dbo.ChatKeyDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatKeyRepositoryImpl(
	private val chatKeyDao: ChatKeyDao,
) : ChatKeyRepository {
	override suspend fun insertKeys(chatId: ChatId, keys: List<Pair<ChatKey, ServerNonce>>) =
		withContext(Dispatchers.IO) {
			val resultingList = keys
				.map {
					ChatKeyDbo(
						chatId = chatId.value,
						key = it.first.value,
						nonce = it.second.value,
					)
				}

			chatKeyDao.insertAll(resultingList)
		}

	override suspend fun insertChatKey(chatId: ChatId, nonce: ServerNonce, key: ChatKey) =
		withContext(Dispatchers.IO) {
			chatKeyDao.set(
				ChatKeyDbo(
					key = key.value,
					chatId = chatId.value,
					nonce = nonce.value,
				)
			)
		}

	override suspend fun getChatKey(chatId: ChatId, nonce: ServerNonce): ChatKey? {
		val chatKey = chatKeyDao.getByNonce(chatId.value, nonce.value)

		return chatKey?.key?.let { ChatKey(it) }
	}

	override suspend fun getLastChatKey(chatId: ChatId): GetLastChatKeyResult? {
		val chatKeyDbo = chatKeyDao.getLatest(chatId.value) ?: return null

		return GetLastChatKeyResult(
			key = ChatKey(chatKeyDbo.key),
			keyNonce = ServerNonce(chatKeyDbo.nonce),
		)
	}

	override suspend fun getOldestChatKey(chatId: ChatId): GetOldestChatKeyResult? {
		val chatKeyDbo = chatKeyDao.getOldest(chatId.value) ?: return null

		return GetOldestChatKeyResult(
			key = ChatKey(chatKeyDbo.key),
			keyNonce = ServerNonce(chatKeyDbo.nonce),
		)
	}
}
