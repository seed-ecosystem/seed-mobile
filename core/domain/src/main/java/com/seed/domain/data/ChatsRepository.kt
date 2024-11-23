package com.seed.domain.data

import com.seed.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
	suspend fun getAll(): Flow<List<Chat>>

	suspend fun add(key: String, name: String)

	suspend fun delete(chatId: String)
}