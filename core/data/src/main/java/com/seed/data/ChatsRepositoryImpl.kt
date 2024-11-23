package com.seed.data

import com.seed.domain.crypto.EncodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat
import com.seed.persistence.dao.ChatDao
import com.seed.persistence.dbo.ChatDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatsRepositoryImpl(
	private val seedCoder: SeedCoder,
	private val chatDao: ChatDao,
) : ChatsRepository {
	override suspend fun getAll(): Flow<List<Chat>> = withContext(Dispatchers.IO) {
		return@withContext chatDao.getAll()
			.map { it.map(ChatDbo::toChat) }
	}

	override suspend fun add(key: String, name: String) = withContext(Dispatchers.IO) {
		val chatIdEncodeResult = seedCoder.encode( // TODO: this is a temp logic that should be fixed
			options = EncodeOptions(
				content = "CHAT_ID",
				key = key
			)
		)

		if (chatIdEncodeResult == null) return@withContext

		chatDao.insert(
			ChatDbo(
				chatId = chatIdEncodeResult.content, // TODO
				chatKey = key,
				chatName = name
			)
		)
	}

	override suspend fun delete(chatId: String) {
		chatDao.deleteById(chatId)
	}
}

private fun ChatDbo.toChat(): Chat = Chat(
	chatId = this.chatId,
	key = this.chatKey,
	name = this.chatName
)
