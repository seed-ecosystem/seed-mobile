package com.seed.domain

import com.seed.domain.values.ChatId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * TODO: Remove this one
 * This is a temporary solution. Normally unread messages count decrease should be served by chat screen
 */
class CurrentChatHandler {
	private val _currentChatState = MutableStateFlow<ChatId?>(null)
	val currentChatState: StateFlow<ChatId?> = _currentChatState

	fun setChat(chatId: ChatId) {
		_currentChatState.update { chatId }
	}

	fun exitChat() {
		_currentChatState.update { null }
	}
}
