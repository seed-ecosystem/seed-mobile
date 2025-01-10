package com.seed.shared.main.presentation.chatlist

import com.seed.core.datetime.now
import com.seed.core.mobile.vm.ViewModel
import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

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
}

private fun Chat.toChatListItem(): ChatListItem {
	return ChatListItem(
		chatId = this.chatId,
		chatName = this.name,
		lastSentMessageDateTime = LocalDateTime.now(), // todo
		lastSentMessageText = "N/A" // todo
	)
}
