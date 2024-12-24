package com.seed.domain.data

import com.seed.domain.api.SocketConnectionState
import com.seed.domain.model.ChatEvent
import com.seed.domain.model.MessageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

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
	val chatUpdatesSharedFlow: SharedFlow<ChatEvent>
	val connectionState: StateFlow<SocketConnectionState>

	suspend fun subscribeToTheChat(coroutineScope: CoroutineScope, chatId: String, nonce: Int)

	suspend fun getMessages(chatId: String): List<MessageContent>

	suspend fun addMessage(chatId: String, message: MessageContent.RegularMessage)

	suspend fun sendMessage(sendMessageDto: SendMessageDto): SendMessageResult
}
