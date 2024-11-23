package com.seed.data

import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatUpdate
import com.seed.domain.data.SendMessageDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

class ChatRepositoryImpl : ChatRepository {
	private val chatUpdatesSharedFlow = MutableSharedFlow<ChatUpdate>()
	private val coroutineScope = CoroutineScope(EmptyCoroutineContext)

	init {
		coroutineScope.launch {
			while (true) {
				generateRandomMessage()

				delay(5000)
			}
		}
	}

	override suspend fun getData(chatId: String): Flow<ChatUpdate> {
		return chatUpdatesSharedFlow
	}

	override suspend fun sendMessage(sendMessageDto: SendMessageDto) {
		generateRandomMessage()
	}

	override suspend fun getChatKey(chatId: String): String {
		return "" // TODO
	}

	private suspend fun generateRandomMessage() {
		chatUpdatesSharedFlow.emit(
			ChatUpdate(
				messageId = "",
				encryptedContentBase64 = "",
				encryptedContentIv = ""
			)
		)
	}
}