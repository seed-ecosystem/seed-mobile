package com.seed.domain.data

import kotlinx.coroutines.flow.Flow

data class SendMessageDto(
	val chatId: String,
	val nonce: Int,
	val encryptedContentBase64: String,
	val encryptedContentIv: String,
	val signature: String,
)

data class ChatUpdate(
	val messageId: String,
	val encryptedContentBase64: String,
	val encryptedContentIv: String,
)

data class GetLastChatKeyResult(
	val key: String,
	val keyNonce: Int
)

interface ChatRepository {
	suspend fun getData(chatId: String): Flow<ChatUpdate>

	suspend fun sendMessage(sendMessageDto: SendMessageDto)

	suspend fun getChatKey(chatId: String, nonce: Int): String?

	suspend fun getLastChatKey(chatId: String): GetLastChatKeyResult?
}