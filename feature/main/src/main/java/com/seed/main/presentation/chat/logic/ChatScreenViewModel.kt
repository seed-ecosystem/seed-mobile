package com.seed.main.presentation.chat.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.Logger
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.WorkerEvent
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.NicknameRepository
import com.seed.domain.model.DecodedChatEvent
import com.seed.domain.model.MessageContent
import com.seed.domain.usecase.SendMessageResult
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import com.seed.domain.usecase.SubscribeToChatUseCaseEvent
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
	private val workerStateHandle: SeedWorkerStateHandle,
	private val nicknameRepository: NicknameRepository,
	private val logger: Logger,
) : ViewModel() {
	private val _state = MutableStateFlow(ChatScreenVmState())

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

		viewModelScope.launch {
			_state.update {
				it.copy(
					isLoading = !workerStateHandle.isWaiting(_state.value.chatId),
					selfNickname = selfNickname,
				)
			}
		}

		viewModelScope.launch {
			subscribeToChatUseCase(_state.value.chatId).collect { event ->
				if (event !is SubscribeToChatUseCaseEvent.New && event !is SubscribeToChatUseCaseEvent.Stored) {
					logger.d(tag = "ChatScreenViewModel", event.toString())
				}

				handleDecodedChatEvent(
					event = event,
					onWaitEvent = onWaitEvent,
					onNewMessage = onNewMessage
				)
			}
		}

		viewModelScope.launch {
			workerStateHandle.connectionState.collect { connectionState ->
				_state.update {
					it.copy(connectionState = connectionState)
				}
			}
		}
	}

	private fun handleDecodedChatEvent(
		event: SubscribeToChatUseCaseEvent,
		onWaitEvent: () -> Unit,
		onNewMessage: () -> Unit
	) {
		when (event) {
			is SubscribeToChatUseCaseEvent.Stored -> {
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

			is SubscribeToChatUseCaseEvent.New -> {
				onNewMessage()

				event.message
					.map { it.toMessage(_state.value.selfNickname) }
					.forEach {
						updateMessagesWithNewMessage(it)
					}
			}

			is SubscribeToChatUseCaseEvent.Wait -> {
				_state.update {
					it.copy(
						isLoading = false
					)
				}

				onWaitEvent()
			}

			is SubscribeToChatUseCaseEvent.Connected -> Unit
			is SubscribeToChatUseCaseEvent.Disconnected -> Unit
			is SubscribeToChatUseCaseEvent.Reconnection -> Unit
			is SubscribeToChatUseCaseEvent.Unknown -> Unit
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
