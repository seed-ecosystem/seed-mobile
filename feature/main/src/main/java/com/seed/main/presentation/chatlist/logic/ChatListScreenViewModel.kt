package com.seed.main.presentation.chatlist.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.KeyManager
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat
import com.seed.domain.usecase.GetChatUrlUseCase
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.LocalDateTime

private data class ChatListScreenVmState(
	val chats: List<ChatListItem>? = null,
	val isError: Boolean = false,
	val isLoading: Boolean = false,
) {
	fun toUiState(): ChatListScreenUiState {
		if (isLoading) return ChatListScreenUiState.Loading
		if (isError) return ChatListScreenUiState.Error

		return if (chats == null) {
			ChatListScreenUiState.Error
		} else {
			if (chats.isEmpty())
				ChatListScreenUiState.NoChats
			else
				ChatListScreenUiState.HasData(chats)
		}
	}
}

class ChatListScreenViewModel(
	private val chatsRepository: ChatsRepository,
	private val getChatUrlUseCase: GetChatUrlUseCase,
) : ViewModel() {
	private val _state = MutableStateFlow(ChatListScreenVmState())

	val state = _state
		.map(ChatListScreenVmState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			ChatListScreenUiState.Loading
		)

	fun loadData() {
		viewModelScope.launch {
			_state.update {
				it.copy(
					isLoading = true
				)
			}

			chatsRepository
				.getAll()
				.map { it.map(Chat::toChatListItem) }
				.collect { chats ->
					_state.update {
						it.copy(isLoading = false, chats = chats)
					}
				}
		}
	}

	suspend fun getChatShareUrl(chatId: String): String? = getChatUrlUseCase(ChatId(chatId))
}

private fun Chat.toChatListItem(): ChatListItem {
	return ChatListItem(
		chatId = this.chatId.value,
		chatName = this.name,
		lastSentMessageDateTime = LocalDateTime.now(), // todo
		lastSentMessageText = "N/A" // todo
	)
}
