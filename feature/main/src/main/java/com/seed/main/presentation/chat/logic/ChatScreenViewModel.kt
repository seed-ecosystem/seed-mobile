package com.seed.main.presentation.chat.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.Logger
import com.seed.domain.model.MessageContent
import com.seed.domain.usecase.DecodedChatEvent
import com.seed.domain.usecase.SendMessageResult
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
			_state.update {
				it.copy(
					isLoading = true
				)
			}

			subscribeToChatUseCase(chatId = _state.value.chatId)
				.catch { handleSubscriptionFlow(it) }
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
									}
								)
							}

							null
						}

						is DecodedChatEvent.New -> {
							event.message.toMessage()
						}

						is DecodedChatEvent.Wait -> {
							_state.update {
								it.copy(
									isLoading = false
								)
							}

							onWaitEvent()

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

	private fun handleSubscriptionFlow(cause: Throwable) {
		logger.e(
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
				chatId = "bHKhl2cuQ01pDXSRaqq/OMJeDFJVNIY5YuQB2w7ve+c=",//_state.value.chatId,
				messageAuthor = "Author", // todo
				messageText = _state.value.inputFieldValue,
				lastMessageNonce =  lastMessageNonce,
			)

			if (sendResult is SendMessageResult.Success) {
//				updateMessagesWithNewMessage(
//					newMessage = Message(
//						nonce = lastMessageNonce + 1,
//						authorType = AuthorType.Self,
//						authorName = "You",
//						messageText = _state.value.inputFieldValue,
//						dateTime = LocalDateTime.now(),
//					)
//				)
				onSuccess()
			}

			_state.update {
				it.copy(inputFieldValue = "")
			}

			onFailure()
		}
	}
}

fun MessageContent.RegularMessage.toMessage(): Message {
	return Message(
		nonce = this.nonce,
		authorType = AuthorType.Others, // todo
		authorName = this.author,
		messageText = this.text.take(100),
		dateTime = LocalDateTime.now() // todo
	)
}