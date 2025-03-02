package com.seed.main.presentation.chatcreate.logic

import com.seed.main.presentation.chatcreate.ServerOption

sealed interface ChatCreateUiState {
	data class Idle(
		val chatName: String = "",
		val chatServerUrl: ServerOption = ServerOption("", ""),
		val isValid: Boolean = false,
	) : ChatCreateUiState

	data object Loading : ChatCreateUiState
}
