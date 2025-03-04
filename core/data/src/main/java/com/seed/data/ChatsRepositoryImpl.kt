package com.seed.data

import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import com.seed.persistence.db.dao.ChatDao
import com.seed.persistence.db.dao.ChatKeyDao
import com.seed.persistence.db.dbo.ChatDbo
import com.seed.persistence.db.dbo.ChatKeyDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatsRepositoryImpl(
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

	override suspend fun getAllServerUrls(): List<ServerUrl> = withContext(Dispatchers.IO) {
		return@withContext chatDao
			.getDistinctServerUrls()
			.map { ServerUrl(it) }
	}

	override suspend fun getChatServerUrl(chatId: ChatId): ServerUrl = withContext(Dispatchers.IO) {
		return@withContext ServerUrl(chatDao.getServerUrlByChatId(chatId.value))
	}

	override suspend fun getChat(chatId: ChatId): Chat? = withContext(Dispatchers.IO) {
		return@withContext chatDao.getById(chatId.value)?.toChat()
	}

	override suspend fun add(
		chatId: ChatId,
		key: ChatKey,
		keyNonce: ServerNonce,
		name: String,
		serverUrl: ServerUrl,
	) {
		withContext(Dispatchers.IO) {
			chatKeyDao.set(
				key = ChatKeyDbo(
					key = key.value,
					nonce = keyNonce.value,
					chatId = chatId.value,
				)
			)

			chatDao.insert(
				ChatDbo(
					chatId = chatId.value,
					chatKey = key.value,
					chatName = name,
					firstChatKeyNonce = keyNonce.value,
					serverUrl = serverUrl.value,
					unreadCount = 0,
				)
			)
		}
	}

	override suspend fun addUnreadCount(count: Int, chatId: ChatId) = withContext(Dispatchers.IO) {
		chatDao.addUnread(count, chatId.value)
	}

	override suspend fun resetUnreadCount(chatId: ChatId) = withContext(Dispatchers.IO) {
		chatDao.resetUnread(chatId.value)
	}

	override suspend fun delete(chatId: ChatId) = withContext(Dispatchers.IO) {
		chatDao.deleteById(chatId.value)
	}
}

private fun ChatDbo.toChat(): Chat = Chat(
	chatId = ChatId(this.chatId),
	name = this.chatName,
	firstChatKeyNonce = ServerNonce(this.firstChatKeyNonce),
	serverUrl = ServerUrl(this.serverUrl),
	unreadCount = this.unreadCount,
)
