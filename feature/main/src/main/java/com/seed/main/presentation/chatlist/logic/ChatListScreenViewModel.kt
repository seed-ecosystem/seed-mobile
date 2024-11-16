package com.seed.main.presentation.chatlist.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class ChatListScreenViewModel : ViewModel() {
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

			delay(300L)

			_state.update {
				it.copy(
					isLoading = false,
					chats = generateRandomChats()
				)
			}
		}
	}
}