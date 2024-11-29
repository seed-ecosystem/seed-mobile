package com.seed.data

import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatUpdate
import com.seed.domain.data.SendMessageDto
import com.seed.persistence.dao.ChatDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

class ChatRepositoryImpl(
	private val chatDao: ChatDao,
	private val messagingApi: SeedMessagingApi,
	private val logger: Logger,
) : ChatRepository {
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
		logger.d(
			tag = "ChatRepository",
			message = "Sending message $sendMessageDto"
		)

		val result = messagingApi.sendMessage(
			chatId = sendMessageDto.chatId,
			content = sendMessageDto.encryptedContentBase64,
			contentIv = sendMessageDto.encryptedContentIv,
			nonce = sendMessageDto.nonce,
			signature = sendMessageDto.signature,
		)

		if (result is ApiResponse.Failure) {
			logger.e(
				tag = "ChatRepository",
				message = "Error while trying to send api send request: $result"
			)
		}
	}

	override suspend fun getChatKey(chatId: String): String? = withContext(Dispatchers.IO) {
		return@withContext chatDao.getById(chatId)?.chatKey
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