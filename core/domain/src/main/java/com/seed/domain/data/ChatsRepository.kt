package com.seed.domain.data

import com.seed.domain.model.Chat
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
	suspend fun getAll(): Flow<List<Chat>>

	suspend fun getAllChatsList(): List<Chat>

	suspend fun getAllServerUrls(): List<ServerUrl>

	suspend fun add(
		chatId: ChatId,
		key: ChatKey,
		keyNonce: ServerNonce,
		name: String,
		serverUrl: ServerUrl,
	)

	suspend fun delete(chatId: ChatId)

	suspend fun getChatServerUrl(chatId: ChatId): ServerUrl

	suspend fun getChat(chatId: ChatId): Chat?

	suspend fun addUnreadCount(count: Int, chatId: ChatId)

	suspend fun resetUnreadCount(chatId: ChatId)
}
