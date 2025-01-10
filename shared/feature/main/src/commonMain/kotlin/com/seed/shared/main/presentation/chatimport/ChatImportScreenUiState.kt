package com.seed.shared.main.presentation.chatimport

sealed interface ChatImportScreenUiState {
	data class Idle(
		val keyValue: String,
	) : ChatImportScreenUiState

	data object Loading : ChatImportScreenUiState

	data object Error : ChatImportScreenUiState
}