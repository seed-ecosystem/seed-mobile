package com.seed.data

import com.seed.domain.crypto.EncodeOptions
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat

class ChatsRepositoryImpl(
	private val seedCoder: SeedCoder
) : ChatsRepository {
	private val chats = mutableListOf<Chat>() // todo

	override suspend fun getAll(): List<Chat> {
		return chats
	}

	override suspend fun add(key: String, name: String) {
//		val chatIdEncodeResult = seedCoder.encode(
//			options = EncodeOptions(
//				content = "CHAT_ID",
//				key = key
//			)
//		)

//		if (chatIdEncodeResult == null) return

		chats.add(
			Chat(
				chatId = "chatIdEncodeResult.content",
				key = key,
				name = name
			)
		)
	}

	override suspend fun delete(chatId: String) {
		chats.removeIf { chat -> chat.chatId == chatId }
	}
}