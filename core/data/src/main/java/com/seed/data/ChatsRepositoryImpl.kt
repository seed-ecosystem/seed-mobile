package com.seed.data

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat
import com.seed.persistence.db.dao.ChatDao
import com.seed.persistence.db.dao.ChatKeyDao
import com.seed.persistence.db.dbo.ChatDbo
import com.seed.persistence.db.dbo.ChatKeyDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatsRepositoryImpl(
	private val seedCoder: SeedCoder,
	private val chatDao: ChatDao,
	private val chatKeyDao: ChatKeyDao,
) : ChatsRepository {
	override suspend fun getAll(): Flow<List<Chat>> = withContext(Dispatchers.IO) {
		return@withContext chatDao.getAll()
			.map { it.map(ChatDbo::toChat) }
	}

	override suspend fun getAllChatsList(): List<Chat> = withContext(Dispatchers.IO) {
		return@withContext chatDao
			.getAllList()
			.map(ChatDbo::toChat)
	}

	override suspend fun add(chatId: String, key: String, keyNonce: Int, name: String) {
		withContext(Dispatchers.IO) {
			chatKeyDao.set(
				key = ChatKeyDbo(
					key = key,
					nonce = keyNonce,
					chatId = chatId,
				)
			)

			chatDao.insert(
				ChatDbo(
					chatId = chatId,
					chatKey = key,
					chatName = name
				)
			)
		}
	}

	override suspend fun delete(chatId: String) = withContext(Dispatchers.IO) {
		chatDao.deleteById(chatId)
	}
}

private fun ChatDbo.toChat(): Chat = Chat(
	chatId = this.chatId,
	name = this.chatName
)
