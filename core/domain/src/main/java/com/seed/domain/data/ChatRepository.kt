package com.seed.domain.data

import com.seed.domain.model.MessageContent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl

data class SendMessageDto(
	val chatId: ChatId,
	val nonce: ServerNonce,
	val encryptedContentBase64: String,
	val encryptedContentIv: String,
	val signature: String,
	val serverUrl: ServerUrl,
)

data class GetLastChatKeyResult(
	val key: ChatKey,
	val keyNonce: ServerNonce,
)

data class GetOldestChatKeyResult(
	val key: ChatKey,
	val keyNonce: ServerNonce,
)

sealed interface SendMessageResult {
	data object Success : SendMessageResult
	data object Failure : SendMessageResult
}

interface ChatRepository {
	suspend fun getMessages(chatId: ChatId): List<MessageContent>

	suspend fun addMessage(chatId: ChatId, message: MessageContent.RegularMessage)

	suspend fun addMessagesList(chatId: ChatId, messages: List<MessageContent.RegularMessage>)
}
