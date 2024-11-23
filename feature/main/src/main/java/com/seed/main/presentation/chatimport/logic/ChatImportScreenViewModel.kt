package com.seed.main.presentation.chatimport.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class ChatImportScreenVmState(
	val keyValue: String = "",
	val isLoading: Boolean = false,
	val isError: Boolean = false
) {
	fun toUiState(): ChatImportScreenUiState {
		if (isLoading) return ChatImportScreenUiState.Loading
		if (isError) return ChatImportScreenUiState.Error

		return ChatImportScreenUiState.Idle(
			keyValue = keyValue
		)
	}
}

class ChatImportScreenViewModel(
	private val chatsRepository: ChatsRepository
) : ViewModel() {
	private val _state = MutableStateFlow(ChatImportScreenVmState())

	val state: StateFlow<ChatImportScreenUiState> = _state
		.map(ChatImportScreenVmState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			ChatImportScreenUiState.Idle("")
		)

	fun updateKeyValue(newValue: String) {
		_state.update {
			it.copy(
				keyValue = newValue
			)
		}
	}

	fun proceedWithKey(
		onSuccess: () -> Unit,
		onFailure: () -> Unit
	) {
		viewModelScope.launch {
			chatsRepository.add(
				key = _state.value.keyValue,
				name = "Chat-${Random.nextInt(1, 1000)}"
			)

			onSuccess()
		}
	}
}