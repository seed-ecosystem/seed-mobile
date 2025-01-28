package com.seed.domain.data

import com.seed.domain.model.MessageContent

data class SendMessageDto(
	val chatId: String,
	val nonce: Int,
	val encryptedContentBase64: String,
	val encryptedContentIv: String,
	val signature: String,
)

data class GetLastChatKeyResult(
	val key: String,
	val keyNonce: Int
)

data class GetOldestChatKeyResult(
	val key: String,
	val keyNonce: Int
)

sealed interface SendMessageResult {
	data object Success : SendMessageResult
	data object Failure : SendMessageResult
}

interface ChatRepository {
	suspend fun getMessages(chatId: String): List<MessageContent>

	suspend fun addMessage(chatId: String, message: MessageContent.RegularMessage)

	suspend fun addMessagesList(chatId: String, messages: List<MessageContent.RegularMessage>)

	suspend fun sendMessage(sendMessageDto: SendMessageDto): SendMessageResult
}
