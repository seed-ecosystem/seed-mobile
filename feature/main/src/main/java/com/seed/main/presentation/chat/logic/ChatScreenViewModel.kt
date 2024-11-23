package com.seed.main.presentation.chat.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.random.Random

private data class ChatScreenVmState(
	val messages: List<Message>? = null,
	val inputFieldValue: String = "",
	val chatName: String = "",
	val chatId: String = "",
	val isLoading: Boolean = false,
	val isError: Boolean = false,
) {
	fun toUiState(): ChatScreenUiState {
		if (isLoading) return ChatScreenUiState.Loading(
			chatName = chatName,
			inputFieldValue = inputFieldValue
		)

		if (isError || messages == null) return ChatScreenUiState.Error(
			chatName = chatName,
			inputFieldValue = inputFieldValue
		)

		if (messages.isEmpty()) return ChatScreenUiState.NoMessages(
			chatName = chatName,
			inputFieldValue = inputFieldValue,
		)

		return ChatScreenUiState.HasData(
			messages = messages,
			chatName = chatName,
			inputFieldValue = inputFieldValue,
		)
	}
}

class ChatScreenViewModel(
	private val chatRepository: ChatRepository,
) : ViewModel() {
	private val _state = MutableStateFlow(ChatScreenVmState())

	val state: StateFlow<ChatScreenUiState> = _state
		.map(ChatScreenVmState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			ChatScreenUiState.Loading("", "")
		)

	fun setInitialData(chatName: String) {
		_state.update {
			it.copy(
				chatName = chatName,
			)
		}
	}

	fun loadData() {
		viewModelScope.launch {
			_state.update {
				it.copy(
					isLoading = true
				)
			}

			chatRepository
				.getData(_state.value.chatId)
				.catch { cause ->
					_state.update {
						it.copy(
							isLoading = false,
							isError = true
						)
					}
				}
				.collect { messages ->
					val newMessage = Message(
						id = Random.nextLong(),
						authorType = AuthorType.Others,
						authorName = "TODO",
						messageText = "TODO()",
						dateTime = LocalDateTime.now()
					)

					val newMessagesList = _state.value.messages?.let { oldMessages ->
						oldMessages + newMessage
					} ?: listOf(newMessage)

					_state.update {
						it.copy(
							isLoading = false,
							isError = false,
							messages = newMessagesList
						)
					}
				}
		}
	}

	fun updateInputValue(newValue: String) {
		_state.update {
			it.copy(
				inputFieldValue = newValue
			)
		}
	}

	fun sendMessage(onSuccess: () -> Unit) {
		if (_state.value.inputFieldValue.isBlank()) return

		_state.update {
			it.copy(
				messages = (_state.value.messages ?: emptyList()) + Message(
					id = (_state.value.messages?.size ?: 0) + 1L,
					authorType = AuthorType.Self,
					authorName = "You",
					messageText = _state.value.inputFieldValue.trim(),
					dateTime = LocalDateTime.now()
				),
				inputFieldValue = ""
			)
		}

		onSuccess()
	}
}