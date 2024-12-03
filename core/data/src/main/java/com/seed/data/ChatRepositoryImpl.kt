package com.seed.data

import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatUpdate
import com.seed.domain.data.GetLastChatKeyResult
import com.seed.domain.data.SendMessageDto
import com.seed.persistence.dao.ChatDao
import com.seed.persistence.dao.ChatKeyDao
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
	private val chatKeyDao: ChatKeyDao,
	private val messagingApi: SeedMessagingApi,
	private val logger: Logger,
) : ChatRepository {
	override val chatUpdatesSharedFlow = messagingApi.chatEvents
	private val coroutineScope = CoroutineScope(EmptyCoroutineContext)

	override suspend fun subscribeToTheChat(chatId: String) {
		val subscriptionResult = messagingApi
			.subscribeToChat(chatId = chatId, nonce = 0)

		logger.d(
			tag = "ChatRepositoryImpl subscribeToTheChat",
			message = "Subscribe success, API result: $subscriptionResult"
		)
	}

	override suspend fun sendMessage(sendMessageDto: SendMessageDto) = withContext(Dispatchers.IO) {
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

	override suspend fun getChatKey(chatId: String, nonce: Int): String? =
		withContext(Dispatchers.IO) {
			val chatKey = chatKeyDao.getByNonce(chatId, nonce)

			return@withContext chatKey?.key
		}

	override suspend fun getLastChatKey(chatId: String): GetLastChatKeyResult? =
		withContext(Dispatchers.IO) {
			val chatKeyDbo = chatKeyDao.getLatest(chatId) ?: return@withContext null

			return@withContext GetLastChatKeyResult(
				key = chatKeyDbo.key,
				keyNonce = chatKeyDbo.nonce
			)
		}
}