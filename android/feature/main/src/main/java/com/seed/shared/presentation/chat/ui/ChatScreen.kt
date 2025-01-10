package com.seed.shared.presentation.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seed.shared.presentation.chat.logic.ChatScreenUiState
import com.seed.shared.presentation.chat.ui.components.ChatScreenTopBar
import com.seed.shared.presentation.chat.ui.components.MessageInputField
import com.seed.shared.presentation.chat.ui.states.ErrorState
import com.seed.shared.presentation.chat.ui.states.HasDataState
import com.seed.shared.presentation.chat.ui.states.LoadingState
import com.seed.shared.presentation.chat.ui.states.NoMessagesState
import kotlinx.coroutines.CoroutineScope

@Composable
fun ChatScreen(
	coroutineScope: CoroutineScope,
	onBackClick: () -> Unit,
	onSend: () -> Unit,
	onInputValueUpdate: (String) -> Unit,
	state: ChatScreenUiState,
	chatBubbleListState: LazyListState,
	modifier: Modifier = Modifier
) {
	Scaffold(
		contentWindowInsets = WindowInsets(0.dp),
		topBar = {
			ChatScreenTopBar(
				chatName = state.chatName,
				connectionState = state.connectionState,
				onBackClick = onBackClick,
			)
		},
		modifier = modifier,
	) { innerPadding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.imePadding()
		) {
			val commonModifier = Modifier
				.fillMaxWidth()
				.weight(1f)

			when (state) {
				is ChatScreenUiState.HasData -> {
					HasDataState(
						state = state,
						chatBubbleListState = chatBubbleListState,
						coroutineScope = coroutineScope,
						modifier = commonModifier
					)
				}

				is ChatScreenUiState.NoMessages -> {
					NoMessagesState(commonModifier)
				}

				is ChatScreenUiState.Loading -> {
					LoadingState(commonModifier)
				}

				is ChatScreenUiState.Error -> {
					ErrorState(commonModifier)
				}
			}

			HorizontalDivider()

			MessageInputField(
				inputValue = state.inputFieldValue,
				onInputValueUpdate = onInputValueUpdate,
				onSend = onSend,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
	}
}