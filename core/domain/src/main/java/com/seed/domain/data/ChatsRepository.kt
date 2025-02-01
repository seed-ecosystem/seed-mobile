package com.seed.domain.data

import com.seed.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
	suspend fun getAll(): Flow<List<Chat>>

	suspend fun getAllChatsList(): List<Chat>

	suspend fun add(chatId: String, key: String, keyNonce: Int, name: String, serverUrl: String)

	suspend fun delete(chatId: String)
}
