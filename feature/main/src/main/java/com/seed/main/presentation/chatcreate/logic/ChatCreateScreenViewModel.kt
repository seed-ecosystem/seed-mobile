package com.seed.main.presentation.chatcreate.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.usecase.CreateChatResult
import com.seed.domain.usecase.CreateChatUseCase
import com.seed.domain.values.ServerUrl
import com.seed.main.presentation.chatcreate.ServerOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class ChatCreateScreenVmState(
	val chatName: String = "",
	val chatServerOption: ServerOption? = null,
	val isLoading: Boolean = false,
) {
	fun toUiState(): ChatCreateUiState {
		if (isLoading) return ChatCreateUiState.Loading

		val isValid = chatName.isNotBlank() && chatServerOption != null

		return ChatCreateUiState.Idle(
			chatName = chatName,
			chatServerUrl = chatServerOption ?: ServerOption("", ""),
			isValid = isValid,
		)
	}
}

class ChatCreateScreenViewModel(
	private val createChatUseCase: CreateChatUseCase,
) : ViewModel() {
	private val _state = MutableStateFlow(ChatCreateScreenVmState())

	val state = _state
		.map(ChatCreateScreenVmState::toUiState)
		.stateIn(viewModelScope, SharingStarted.Eagerly, ChatCreateUiState.Idle())

	fun updateChatName(new: String) {
		_state.update {
			it.copy(chatName = new)
		}
	}

	fun updateServerUrl(new: ServerOption) {
		_state.update {
			it.copy(chatServerOption = new)
		}
	}

	fun createChat(onSuccess: () -> Unit, onFailure: () -> Unit) {
		viewModelScope.launch {
			_state.update {
				it.copy(isLoading = true)
			}

			val chatServerOption = _state.value.chatServerOption

			if (_state.value.chatName.isBlank() || chatServerOption == null) {
				_state.update { it.copy(isLoading = false) }
				onFailure()
				return@launch
			}


			val result = createChatUseCase(
				chatName = _state.value.chatName,
				serverUrl = ServerUrl(chatServerOption.serverUrl)
			)

			if (result is CreateChatResult.Failure) {
				_state.update { it.copy(isLoading = false) }

				onFailure()
			} else {
				_state.update { it.copy(isLoading = false) }

				onSuccess()
			}
		}
	}
}
