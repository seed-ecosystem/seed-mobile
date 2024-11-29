package com.seed.main.presentation.chat.logic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.Logger
import com.seed.domain.model.MessageContent
import com.seed.domain.usecase.GetChatHistoryUseCase
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
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
	private val subscribeToChatUseCase: SubscribeToChatUseCase,
	private val sendMessageUseCase: SendMessageUseCase,
	private val getChatHistoryUseCase: GetChatHistoryUseCase,
	private val logger: Logger,
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

			val result = getChatHistoryUseCase()

			logger.d(
				tag = "ChatScreenViewModel loadData",
				message = "getChatHistoryUseCase result: $result"
			)

			if (result != null) {
				_state.update {
					it.copy(
						messages = result
							.mapNotNull { messageContent ->
								if (messageContent is MessageContent.RegularMessage) {
									return@mapNotNull messageContent.toMessage()
								} else return@mapNotNull null
							}
					)
				}
			}

			subscribeToChatUseCase(chatId = _state.value.chatId)
				.catch { cause ->
					Log.e(
						"ChatScreenViewModel",
						"An error occured: ${cause.localizedMessage}"
					)

					_state.update {
						it.copy(
							isLoading = false,
							isError = true
						)
					}
				}
				.collect { newMessageContent ->
					val newMessage = when (newMessageContent) {
						is MessageContent.RegularMessage -> {
							Message(
								nonce = newMessageContent.nonce,
								authorType = AuthorType.Others, // todo
								authorName = newMessageContent.author,
								messageText = newMessageContent.text,
								dateTime = LocalDateTime.now() // todo
							)
						}

						is MessageContent.UnknownMessage -> {
							Message(
								nonce = Random.nextInt(),
								authorType = AuthorType.Others,
								authorName = "Unknown",
								messageText = "Unknown message",
								dateTime = LocalDateTime.now()
							)
						}
					}

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
		viewModelScope.launch {
			if (_state.value.inputFieldValue.isBlank()) return@launch

			sendMessageUseCase(
				_state.value.chatId,
				messageAuthor = "Author", // todo
				messageText = _state.value.inputFieldValue
			)

			_state.update {
				it.copy(inputFieldValue = "")
			}

			onSuccess()
		}
	}
}

fun MessageContent.RegularMessage.toMessage(): Message {
	return Message(
		nonce = this.nonce,
		authorType = AuthorType.Others, // todo
		authorName = this.author,
		messageText = this.text,
		dateTime = LocalDateTime.now() // todo
	)
}