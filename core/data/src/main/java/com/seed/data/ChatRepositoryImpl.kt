package com.seed.data

import com.seed.domain.Logger
import com.seed.domain.api.ApiResponse
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.SendMessageDto
import com.seed.domain.data.SendMessageResult
import com.seed.domain.model.ChatEvent
import com.seed.domain.model.MessageContent
import com.seed.persistence.db.dao.ChatEventDao
import com.seed.persistence.db.dbo.ChatEventDbo
import com.seed.persistence.db.dbo.ChatEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

class ChatRepositoryImpl(
	private val chatEventDao: ChatEventDao,
	private val messagingApi: SeedMessagingApi,
	private val logger: Logger,
) : ChatRepository {
	override val chatUpdatesSharedFlow: SharedFlow<ChatEvent> = messagingApi.chatEvents
	override val connectionState = messagingApi.connectionState

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

	override suspend fun getMessages(chatId: String): List<MessageContent> =
		chatEventDao.getAll()
			.map {
				MessageContent.RegularMessage(
					nonce = it.nonce,
					title = it.title,
					text = it.text,
				)
			}

	override suspend fun addMessage(chatId: String, message: MessageContent.RegularMessage) {
		chatEventDao.insert(
			message.toChatEventDbo(chatId)
		)
	}

	override suspend fun sendMessage(sendMessageDto: SendMessageDto): SendMessageResult {
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

			return SendMessageResult.Failure
		}

		return SendMessageResult.Success
	}
}

private fun MessageContent.RegularMessage.toChatEventDbo(chatId: String): ChatEventDbo {
	return ChatEventDbo(
		chatId = chatId,
		nonce = this.nonce,
		eventType = ChatEventType.NewMessage,
		title = this.title,
		this.text,
	)
}
