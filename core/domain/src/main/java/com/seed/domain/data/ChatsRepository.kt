package com.seed.domain.data

import com.seed.domain.model.Chat
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
	suspend fun getAll(): Flow<List<Chat>>

	suspend fun getAllChatsList(): List<Chat>

	suspend fun getAllServerUrls(): List<String>

	suspend fun add(chatId: String, key: String, keyNonce: Int, name: String, serverUrl: String)

	suspend fun delete(chatId: String)
	suspend fun getChatServerUrl(chatId: ChatId): ServerUrl
}
