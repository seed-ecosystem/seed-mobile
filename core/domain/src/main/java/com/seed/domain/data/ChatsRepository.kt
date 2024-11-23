package com.seed.domain.data

import com.seed.domain.model.Chat

interface ChatsRepository {
	suspend fun getAll(): List<Chat>

	suspend fun add(key: String, name: String)

	suspend fun delete(chatId: String)
}