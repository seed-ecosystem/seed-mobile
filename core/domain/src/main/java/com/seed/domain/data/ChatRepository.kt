package com.seed.domain.data

import com.seed.domain.model.ChatEvent
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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

data class GetOldestChatKeyResult(
	val key: String,
	val keyNonce: Int
)

interface ChatRepository {
	val chatUpdatesSharedFlow: SharedFlow<ChatEvent>

	suspend fun subscribeToTheChat(coroutineScope: CoroutineScope, chatId: String, nonce: Int)

	suspend fun getMessages(chatId: String): Flow<MessageContent>

	suspend fun sendMessage(sendMessageDto: SendMessageDto)

	suspend fun getChatKey(chatId: String, nonce: Int): String?

	suspend fun getLastChatKey(chatId: String): GetLastChatKeyResult?

	suspend fun getOldestChatKey(chatId: String): GetOldestChatKeyResult?

	suspend fun insertChatKey(chatId: String, nonce: Int, key: String)
	suspend fun launchConnection(coroutineScope: CoroutineScope)
}