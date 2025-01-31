package com.seed.main.presentation.chat.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.Logger
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.api.SocketConnectionState
import com.seed.domain.data.NicknameRepository
import com.seed.domain.model.MessageContent
import com.seed.domain.usecase.SendMessageResult
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import com.seed.domain.usecase.SubscribeToChatUseCaseEvent
import kotlinx.coroutines.flow.MutableStateFlow
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

data class ChatScreenViewModelOptions(
	val chatName: String,
	val chatId: String,
)

class ChatScreenViewModel(
	private val options: ChatScreenViewModelOptions,
	private val subscribeToChatUseCase: SubscribeToChatUseCase,
	private val sendMessageUseCase: SendMessageUseCase,
	private val workerStateHandle: SeedWorkerStateHandle,
	private val nicknameRepository: NicknameRepository,
	private val logger: Logger,
) : ViewModel() {
	private val _state = MutableStateFlow(ChatScreenVmState(chatName = options.chatName))

	val state: StateFlow<ChatScreenUiState> = _state
		.map(ChatScreenVmState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			ChatScreenUiState.Loading("", "", SocketConnectionState.DISCONNECTED)
		)

	fun loadData(
		onWaitEvent: () -> Unit,
		onNewMessage: () -> Unit,
	) {
		val selfNickname = nicknameRepository.getNickname()

		viewModelScope.launch {
			_state.update {
				it.copy(
					isLoading = !workerStateHandle.isWaiting(options.chatId),
					selfNickname = selfNickname,
				)
			}
		}

		viewModelScope.launch {
			subscribeToChatUseCase(options.chatId).collect { event ->
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
				val messages = event.messages
					.mapNotNull { message ->
						return@mapNotNull when (message) {
							is MessageContent.RegularMessage -> message

							else -> null
						}
					}
					.mapIndexed { index: Int, message: MessageContent.RegularMessage ->
						message.toMessage(
							localNonce = index,
							_state.value.selfNickname
						)
					}
					.reversed()

				_state.update {
					it.copy(
						messages = messages,
					)
				}
			}

			is SubscribeToChatUseCaseEvent.New -> {
				onNewMessage()

				event.message
					.map { it.toMessage(getLastLocalNonce() + 1, _state.value.selfNickname) }
					.forEach {
						addNewMessage(it)
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

	private fun getLastLocalNonce(): Int {
		return _state.value.messages
			?.maxBy { it.localNonce }
			?.localNonce
			?: 0
	}

	private fun addNewMessage(new: Message) {
		if (_state.value.messages?.any { it.serverNonce == new.serverNonce } == true) return

		val newMessages = _state.value.messages?.let { oldMessages ->
			listOf(new) + oldMessages
		} ?: listOf(new)

		_state.update { it.copy(messages = newMessages) }
	}

	fun updateInputValue(newValue: String) {
		_state.update {
			it.copy(
				inputFieldValue = newValue
			)
		}
	}

	fun sendMessage(onMessageAdd: () -> Unit) {
		if (_state.value.inputFieldValue.isBlank()) return

		val messageText = _state.value.inputFieldValue
		val messageLocalNonce = getLastLocalNonce() + 1

		_state.update { it.copy(inputFieldValue = "") }

		addNewMessage(
			Message.SelfMessage(
				localNonce = messageLocalNonce,
				serverNonce = null,
				authorName = "",
				messageText = messageText,
				dateTime = LocalDateTime.now(), // TODO
				isSending = true,
				isSendFailed = false
			)
		)

		viewModelScope.launch {
			val sendResult = sendMessageUseCase(
				chatId = options.chatId,
				messageText = messageText,
			)

			updateMessageSendState(
				localNonce = messageLocalNonce,
				serverNonce = if (sendResult is SendMessageResult.Success) sendResult.newServerNonce else null,
				isFailed = sendResult is SendMessageResult.Failure
			)
		}

		onMessageAdd()
	}

	private fun updateMessageSendState(localNonce: Int, serverNonce: Int?, isFailed: Boolean) {
		val messages = _state.value.messages

		val newMessages = messages?.map { message ->
			if (message is Message.SelfMessage && message.localNonce == localNonce) {
				message.copy(
					serverNonce = serverNonce,
					isSending = false,
					isSendFailed = isFailed,
				)
			} else message
		}

		_state.update {
			it.copy(
				messages = newMessages
			)
		}
	}
}

fun MessageContent.RegularMessage.toMessage(
	localNonce: Int,
	selfNickname: String?,
): Message {
	return if (this.title == selfNickname) Message.SelfMessage(
		localNonce = localNonce,
		serverNonce = this.nonce,
		authorName = this.title,
		messageText = this.text,
		dateTime = LocalDateTime.now(), // todo
		isSending = false,
		isSendFailed = false,
	) else Message.OthersMessage(
		localNonce = localNonce,
		serverNonce = this.nonce,
		authorName = this.title,
		messageText = this.text,
		dateTime = LocalDateTime.now() // todo
	)
}
