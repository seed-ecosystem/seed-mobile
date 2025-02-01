package com.seed.main.presentation.chatimport.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.usecase.AddChatUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder

private data class ChatImportScreenVmState(
	val keyValue: String = "",
	val isLoading: Boolean = false,
	val isError: Boolean = false
) {
	fun toUiState(): ChatImportScreenUiState {
		if (isLoading) return ChatImportScreenUiState.Loading
		if (isError) return ChatImportScreenUiState.Error

		return ChatImportScreenUiState.Idle(
			keyValue = keyValue
		)
	}
}

class ChatImportScreenViewModel(
	private val addChatUseCase: AddChatUseCase,
) : ViewModel() {
	private val _state = MutableStateFlow(ChatImportScreenVmState())

	val state: StateFlow<ChatImportScreenUiState> = _state
		.map(ChatImportScreenVmState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			ChatImportScreenUiState.Idle("")
		)

	fun updateKeyValue(newValue: String) {
		_state.update {
			it.copy(
				keyValue = newValue
			)
		}
	}

	fun proceedWithKey(
		onSuccess: () -> Unit,
		onFailure: () -> Unit
	) {
		try {
			val parsedChat = parseChatUri(_state.value.keyValue)

			viewModelScope.launch {
				addChatUseCase(
					key = parsedChat.privateKey,
					keyNonce = parsedChat.nonce,
					name = parsedChat.chatName,
					chatId = parsedChat.chatId,
					serverUrl = parsedChat.serverAddress
				)

				onSuccess()
			}
		} catch (ex: IllegalArgumentException) {
			onFailure()
			return
		}
	}
}

data class ParsedChatUri(
	val chatName: String,
	val privateKey: String,
	val nonce: Int,
	val chatId: String,
	val serverAddress: String
)

fun parseChatUri(encodedUri: String): ParsedChatUri {
	val parts = encodedUri.split("#/import/").getOrNull(1)?.split("/")
		?: throw IllegalArgumentException("Invalid URI format")

	if (parts.size != 5) {
		throw IllegalArgumentException("URI does not contain the expected number of components")
	}

	val chatName = URLDecoder.decode(parts[0], "UTF-8")
	val chatId = URLDecoder.decode(parts[1], "UTF-8")
	val privateKey = URLDecoder.decode(parts[2], "UTF-8")
	val nonce = URLDecoder.decode(parts[3], "UTF-8")
	val serverAddress = URLDecoder.decode(parts[4], "UTF-8")

	return ParsedChatUri(
		chatName = chatName,
		privateKey = privateKey,
		nonce = nonce.toInt(),
		chatId = chatId,
		serverAddress = serverAddress
	)
}
