package com.seed.data

import com.seed.domain.Logger
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.MessageContent
import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.persistence.db.dao.ChatEventDao
import com.seed.persistence.db.dbo.ChatEventDbo
import com.seed.persistence.db.dbo.ChatEventType

class ChatRepositoryImpl(
	private val chatEventDao: ChatEventDao,
	private val logger: Logger,
) : ChatRepository {
	override suspend fun getMessages(chatId: ChatId): List<MessageContent> =
		chatEventDao.getAllByChatId(chatId.value)
			.map {
				MessageContent.RegularMessage(
					nonce = ServerNonce(it.nonce),
					title = it.title,
					text = it.text,
					receiveTimestamp = it.receiveTimestamp,
				)
			}

	override suspend fun addMessage(chatId: ChatId, message: MessageContent.RegularMessage) {
		chatEventDao.insert(
			message.toChatEventDbo(chatId)
		)
	}

	override suspend fun addMessagesList(
		chatId: ChatId,
		messages: List<MessageContent.RegularMessage>
	) {
		chatEventDao.insertAll(
			messages.map { it.toChatEventDbo(chatId) }
		)
	}
}

private fun MessageContent.RegularMessage.toChatEventDbo(chatId: ChatId): ChatEventDbo {
	return ChatEventDbo(
		chatId = chatId.value,
		nonce = this.nonce.value,
		eventType = ChatEventType.NewMessage,
		title = this.title,
		text = this.text,
		receiveTimestamp = this.receiveTimestamp,
	)
}
