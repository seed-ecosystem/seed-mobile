package com.seed.shared.presentation.chatlist.logic

import java.time.LocalDateTime

data class ChatListItem(
	val chatId: String,
	val chatName: String,
	val lastSentMessageDateTime: LocalDateTime,
	val lastSentMessageText: String,
)

sealed interface ChatListScreenUiState {
	data class HasData(
		val chats: List<ChatListItem>
	) : ChatListScreenUiState

	data object NoChats : ChatListScreenUiState

	data object Loading : ChatListScreenUiState

	data object Error : ChatListScreenUiState
}
