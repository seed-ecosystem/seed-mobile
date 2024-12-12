package com.seed.main.presentation.chat.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.Logger
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.data.ChatRepository
import com.seed.domain.model.DecodedChatEvent
import com.seed.domain.model.MessageContent
import com.seed.domain.usecase.SendMessageResult
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private data class ChatScreenVmState(
	val messages: List<Message>? = null,
	val inputFieldValue: String = "",
	val chatName: String = "",
	val chatId: String = "",
	val isLoading: Boolean = false,
	val isError: Boolean = false,
	val connectionState: SocketConnectionState = SocketConnectionState.DISCONNECTED,
) {
	fun toUiState(): ChatScreenUiState {
		if (isLoading) return ChatScreenUiState.Loading(
			chatName = chatName,
			inputFieldValue = inputFieldValue,
			connectionState = connectionState,
		)

		if (isError || messages == null) return ChatScreenUiState.Error(
			chatName = chatName,
			inputFieldValue = inputFieldValue,
			connectionState = connectionState,
		)

		if (messages.isEmpty()) return ChatScreenUiState.NoMessages(
			chatName = chatName,
			inputFieldValue = inputFieldValue,
			connectionState = connectionState,
		)

		return ChatScreenUiState.HasData(
			messages = messages ?: emptyList(),
			chatName = chatName,
			inputFieldValue = inputFieldValue,
			connectionState = connectionState,
		)
	}
}

class ChatScreenViewModel(
	private val subscribeToChatUseCase: SubscribeToChatUseCase,
	private val sendMessageUseCase: SendMessageUseCase,
	private val chatRepository: ChatRepository,
	private val logger: Logger,
) : ViewModel() {
	private val _state = MutableStateFlow(ChatScreenVmState())

	private val _debugEvents = MutableSharedFlow<String>()
	val debugEvents: SharedFlow<String> = _debugEvents

	val state: StateFlow<ChatScreenUiState> = _state
		.map(ChatScreenVmState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			ChatScreenUiState.Loading("", "", SocketConnectionState.DISCONNECTED)
		)

	fun setInitialData(chatName: String, chatId: String) {
		_state.update {
			it.copy(
				chatName = chatName,
				chatId = chatId
			)
		}
	}

	fun loadData(
		onWaitEvent: () -> Unit,
		onNewMessage: () -> Unit,
	) {
		viewModelScope.launch {
			chatRepository.connectionState.collect { connectionState ->
				_state.update {
					it.copy(connectionState = connectionState)
				}
			}
		}

		viewModelScope.launch {
			_state.update {
				it.copy(
					isLoading = true
				)
			}

			subscribeToChatUseCase(chatId = _state.value.chatId, scope = viewModelScope)
				.collect { event ->
					val newMessage: Message? = when (event) {
						is DecodedChatEvent.Stored -> {
							_state.update {
								it.copy(
									messages = event.messages.mapNotNull { message ->
										return@mapNotNull when (message) {
											is MessageContent.RegularMessage -> {
												message.toMessage()
											}

											else -> null
										}
									},
									isLoading = false,
								)
							}

							null
						}

						is DecodedChatEvent.New -> {
							event.message.toMessage()
						}

						is DecodedChatEvent.Wait -> {
							onWaitEvent()

							_debugEvents.emit("Wait")

							null
						}

						is DecodedChatEvent.Connected -> {
							_debugEvents.emit("Connected")

							null
						}

						is DecodedChatEvent.Disconnected -> {
							_debugEvents.emit("Disconnected")

							null
						}

						is DecodedChatEvent.Reconnection -> {
							_debugEvents.emit("Reconnection")

							null
						}

						else -> null
					}

					newMessage?.let {
						onNewMessage()
						updateMessagesWithNewMessage(it)
					}
				}
		}
	}

	private fun updateMessagesWithNewMessage(newMessage: Message) {
		if (_state.value.messages?.any { newMessage.nonce == it.nonce } == true) {
			return
		}

		val newMessageList = _state.value.messages?.let { oldMessages ->
			oldMessages + listOf(newMessage)
		} ?: listOf(newMessage)

		_state.update {
			it.copy(
				isLoading = false,
				isError = false,
				messages = newMessageList,
			)
		}
	}

	fun updateInputValue(newValue: String) {
		_state.update {
			it.copy(
				inputFieldValue = newValue
			)
		}
	}

	fun sendMessage(onSuccess: () -> Unit, onFailure: () -> Unit) {
		viewModelScope.launch {
			if (_state.value.inputFieldValue.isBlank()) return@launch

			val lastMessageNonce = _state.value.messages?.last()?.nonce ?: return@launch

			val sendResult = sendMessageUseCase(
				chatId = _state.value.chatId,
				messageAuthor = "Author", // todo
				messageText = _state.value.inputFieldValue,
				lastMessageNonce = lastMessageNonce,
			)

			if (sendResult is SendMessageResult.Success) {
				onSuccess()
			}

			_state.update {
				it.copy(inputFieldValue = "")
			}

			onFailure()
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	override fun onCleared() {
		GlobalScope.launch {
			chatRepository.stopConnection()

			super.onCleared()

			logger.d(tag = "ChatScreenViewModel", message = "Cleared")
		}
	}
}

fun MessageContent.RegularMessage.toMessage(): Message {
	return Message(
		nonce = this.nonce,
		authorType = AuthorType.Others, // todo
		authorName = this.title,
		messageText = this.text,
		dateTime = LocalDateTime.now() // todo
	)
}