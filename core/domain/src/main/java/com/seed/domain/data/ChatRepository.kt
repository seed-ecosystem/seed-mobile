package com.seed.domain.data

import kotlinx.coroutines.flow.Flow

data class SendMessageDto(
	val chatId: String,
	val messageId: String,
	val encryptedContentBase64: String,
	val encryptedContentIv: String,
)

data class ChatUpdate(
	val messageId: String,
	val encryptedContentBase64: String,
	val encryptedContentIv: String,
)

interface ChatRepository {
	suspend fun getData(chatId: String): Flow<ChatUpdate>

	suspend fun sendMessage(sendMessageDto: SendMessageDto)

	suspend fun getChatKey(chatId: String): String? // todo this should return nonce of the key also
}