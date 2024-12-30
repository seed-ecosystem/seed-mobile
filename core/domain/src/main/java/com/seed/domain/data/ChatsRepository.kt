package com.seed.domain.data

import com.seed.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
	suspend fun getAll(): Flow<List<Chat>>

	suspend fun getAllChatsList(): List<Chat>

	suspend fun add(key: String, keyNonce: Int, name: String)

	suspend fun delete(chatId: String)
}
