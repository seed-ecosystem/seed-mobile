package com.seed.domain.data

import com.seed.domain.model.ChatEvent
import kotlinx.coroutines.flow.SharedFlow

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
	val chatUpdatesSharedFlow: SharedFlow<ChatEvent>

	suspend fun subscribeToTheChat(chatId: String)

	suspend fun sendMessage(sendMessageDto: SendMessageDto)

	suspend fun getChatKey(chatId: String, nonce: Int): String?

	suspend fun getLastChatKey(chatId: String): GetLastChatKeyResult?
}