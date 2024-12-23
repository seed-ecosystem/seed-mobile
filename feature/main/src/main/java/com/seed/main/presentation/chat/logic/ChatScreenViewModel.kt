package com.seed.main.presentation.chat.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.Logger
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.NicknameRepository
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
	val selfNickname: String? = null,
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
			messages = messages,
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
	private val nicknameRepository: NicknameRepository,
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
				chatId = chatId,
			)
		}
	}

	fun loadData(
		onWaitEvent: () -> Unit,
		onNewMessage: () -> Unit,
	) {
		val selfNickname = nicknameRepository.getNickname()

		_state.update {
			it.copy(
				isLoading = true,
				selfNickname = selfNickname,
			)
		}

		viewModelScope.launch {
			chatRepository.connectionState.collect { connectionState ->
				_state.update {
					it.copy(connectionState = connectionState)
				}
			}
		}

		viewModelScope.launch {
			subscribeToChatUseCase(chatId = _state.value.chatId, scope = viewModelScope)
				.collect { event ->
					handleDecodedChatEvent(event, onWaitEvent, onNewMessage)
				}
		}
	}

	private fun handleDecodedChatEvent(
		event: DecodedChatEvent,
		onWaitEvent: () -> Unit,
		onNewMessage: () -> Unit
	) {
		when (event) {
			is DecodedChatEvent.Stored -> {
				_state.update {
					it.copy(
						messages = event.messages.mapNotNull { message ->
							return@mapNotNull when (message) {
								is MessageContent.RegularMessage -> {
									message.toMessage(_state.value.selfNickname)
								}

								else -> null
							}
						}.reversed(),
					)
				}
			}

			is DecodedChatEvent.New -> {
				onNewMessage()
				updateMessagesWithNewMessage(event.message.toMessage(_state.value.selfNickname))
			}

			is DecodedChatEvent.Wait -> {
				_state.update {
					it.copy(
						isLoading = false
					)
				}

				onWaitEvent()
			}

			is DecodedChatEvent.Connected -> Unit

			is DecodedChatEvent.Disconnected -> Unit

			is DecodedChatEvent.Reconnection -> Unit

			is DecodedChatEvent.Unknown -> Unit
		}
	}

	private fun updateMessagesWithNewMessage(newMessage: Message) {
		if (_state.value.messages?.any { newMessage.nonce == it.nonce } == true) {
			return
		}

		val newMessageList = _state.value.messages?.let { oldMessages ->
			listOf(newMessage) + oldMessages
		} ?: listOf(newMessage)

		_state.update {
			it.copy(
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

			val lastMessageNonce = getLastMessageNonce() ?: return@launch
			val newMessageNonce = lastMessageNonce + 1
			val messageText = _state.value.inputFieldValue

			updateMessagesWithNewMessage(
				newMessage = Message.SelfMessage(
					nonce = newMessageNonce,
					authorName = "",
					messageText = messageText,
					dateTime = LocalDateTime.now(),
					isSending = true,
					isSendFailed = false,
				)
			)

			viewModelScope.launch {
				val sendMessageResult = sendMessageUseCase(
					chatId = _state.value.chatId,
					messageText = messageText,
					lastMessageNonce = lastMessageNonce,
				)

				updateMessageSendState(
					nonce = newMessageNonce,
					isFailed = sendMessageResult is SendMessageResult.Failure
				)
			}

			_state.update {
				it.copy(inputFieldValue = "")
			}

			onSuccess()
		}
	}

	private fun getLastMessageNonce() = _state.value.messages?.first {
		val isSendFailed = if (it is Message.SelfMessage) it.isSendFailed else false
		!isSendFailed
	}?.nonce

	private fun updateMessageSendState(nonce: Int, isFailed: Boolean) {
		_state.update {
			val messages = it.messages
			val newMessages = messages?.map { message ->
				if (message is Message.SelfMessage && message.nonce == nonce) {
					message.copy(
						isSending = false,
						isSendFailed = isFailed,
					)
				} else message
			}

			it.copy(
				messages = newMessages
			)
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	override fun onCleared() {
		GlobalScope.launch {
			chatRepository.stopConnection()

			super.onCleared()
		}
	}
}

fun MessageContent.RegularMessage.toMessage(selfNickname: String?): Message {
	return if (this.title == selfNickname) Message.SelfMessage(
		nonce = this.nonce,
		authorName = this.title,
		messageText = this.text,
		dateTime = LocalDateTime.now(), // todo
		isSending = false,
		isSendFailed = false,
	) else Message.OthersMessage(
		nonce = this.nonce,
		authorName = this.title,
		messageText = this.text,
		dateTime = LocalDateTime.now() // todo
	)
}