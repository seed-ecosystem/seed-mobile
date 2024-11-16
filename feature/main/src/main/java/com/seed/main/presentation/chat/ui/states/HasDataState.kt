package com.seed.main.presentation.chat.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.seed.main.presentation.chat.logic.ChatScreenUiState
import com.seed.main.presentation.chat.ui.components.ChatBubbleList

@Composable
fun HasDataState(
	state: ChatScreenUiState.HasData,
	chatBubbleListState: LazyListState,
	modifier: Modifier = Modifier
) {
	Box(modifier) {
		ChatBubbleList(
			state = chatBubbleListState,
			messages = state.messages,
			modifier = Modifier.fillMaxSize()
		)
	}
}