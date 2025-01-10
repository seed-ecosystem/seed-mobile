package com.seed.shared.presentation.chatimport.logic

sealed interface ChatImportScreenUiState {
	data class Idle(
		val keyValue: String,
	) : ChatImportScreenUiState

	data object Loading : ChatImportScreenUiState

	data object Error : ChatImportScreenUiState
}