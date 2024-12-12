package com.seed.data

import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.SendMessageDto
import com.seed.domain.model.MessageContent
import com.seed.persistence.dao.ChatDao
import com.seed.persistence.dao.MessageDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatRepositoryImpl(
	private val chatDao: ChatDao,
	private val messageDao: MessageDao,
	private val messagingApi: SeedMessagingApi,
	private val logger: Logger,
) : ChatRepository {
	override val chatUpdatesSharedFlow = messagingApi.chatEvents
	override val connectionState = messagingApi.connectionState

	override suspend fun launchConnection(coroutineScope: CoroutineScope) {
		messagingApi.launchConnection(coroutineScope)
	}

	override suspend fun stopConnection() {
		messagingApi.stopConnection()
	}

	override suspend fun subscribeToTheChat(
		coroutineScope: CoroutineScope,
		chatId: String,
		nonce: Int
	) {
		logger.d(tag = "ChatRepository", message = "subscribed")

		val subscriptionResult = messagingApi
			.subscribeToChat(chatId = chatId, nonce = nonce)

		logger.d(
			tag = "ChatRepository",
			message = "subscribeToTheChat: Subscribe success: $subscriptionResult"
		)
	}

	override suspend fun getMessages(chatId: String): Flow<MessageContent> =
		withContext(Dispatchers.IO) {
			messageDao.getAll()
				.map {
					MessageContent.RegularMessage(
						nonce = it.nonce,
						author = it.title,
						text = it.text,
					)
				}
		}

	override suspend fun sendMessage(sendMessageDto: SendMessageDto) = withContext(Dispatchers.IO) {
		logger.d(
			tag = "ChatRepository",
			message = "sendMessage: Sending message $sendMessageDto"
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
				message = "sendMessage: Error while trying to send api send request: $result"
			)
		}
	}
}