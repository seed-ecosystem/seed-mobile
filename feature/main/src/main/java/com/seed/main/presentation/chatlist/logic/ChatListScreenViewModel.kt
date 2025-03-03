package com.seed.main.presentation.chatlist.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.model.Chat
import com.seed.domain.model.MessageContent
import com.seed.domain.usecase.GetChatUrlUseCase
import com.seed.domain.values.ChatId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private data class ChatListScreenVmState(
	val chats: ChatListState? = null,
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

data class LastSentMessage(
	val author: String?,
	val text: String,
	val receiveTimestamp: Long,
)

data class ChatState(
	val chatId: ChatId,
	val name: String,
	val lastSentMessage: LastSentMessage?,
	val unreadCount: Int = 0, // TODO
)

typealias ChatListState = List<ChatState>

class GetChatListUseCase(
	private val chatsRepository: ChatsRepository,
	private val chatRepository: ChatRepository,
) {
	private val _state = MutableStateFlow<ChatListState>(
		emptyList()
	)

	val state: StateFlow<ChatListState> = _state

	suspend operator fun invoke() {
		chatRepository.getAllMessagesFlow().collectLatest {
			chatsRepository.getAll().collectLatest { chats ->
				val chatStates = chats.map {
					val last = getLastChatMessage(it.chatId)
					ChatState(
						chatId= it.chatId,
						name = it.name,
						lastSentMessage = last,
					)
				}

				_state.update { chatStates.sortedByDescending { it.lastSentMessage?.receiveTimestamp } }
			}
		}
	}

	private suspend fun getLastChatMessage(chatId: ChatId): LastSentMessage? {
		return when (val lastMessage = chatRepository.getLastMessage(chatId)) {
			is MessageContent.RegularMessage -> {
				LastSentMessage(
					author = lastMessage.title,
					text = lastMessage.text,
					receiveTimestamp = lastMessage.receiveTimestamp
				)
			}

			is MessageContent.UnknownMessage -> {
				LastSentMessage(
					author = null,
					text = "N/A",
					receiveTimestamp = lastMessage.receiveTimestamp
				)
			}

			null -> null
		}
	}
}

class ChatListScreenViewModel(
	private val getChatList: GetChatListUseCase,
	private val chatsRepository: ChatsRepository,
	private val getChatUrlUseCase: GetChatUrlUseCase,
) : ViewModel() {
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

			launch { getChatList() }

			getChatList.state
				.collectLatest { chats ->
					_state.update { 
						it.copy(
							isLoading = false,
							chats = chats,
						)
					}
				}
			
//			chatsRepository
//				.getAll()
//				.map { it.map(Chat::toChatListItem) }
//				.collect { chats ->
//					_state.update {
//						it.copy(isLoading = false, chats = chats)
//					}
//				}
		}
	}

	suspend fun getChatShareUrl(chatId: String): String? = getChatUrlUseCase(ChatId(chatId))
}

private fun Chat.toChatListItem(): ChatListItem {
	return ChatListItem(
		chatId = this.chatId.value,
		chatName = this.name,
		lastSentMessageDateTime = LocalDateTime.now(), // todo
		lastSentMessageText = "N/A" // todo
	)
}
