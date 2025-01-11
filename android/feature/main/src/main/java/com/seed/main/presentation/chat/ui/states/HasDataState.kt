package com.seed.main.presentation.chat.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.seed.main.presentation.chat.logic.ChatScreenUiState
import com.seed.main.presentation.chat.ui.components.ChatBubbleList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HasDataState(
	state: ChatScreenUiState.HasData,
	chatBubbleListState: LazyListState,
	coroutineScope: CoroutineScope,
	modifier: Modifier = Modifier
) {
	LaunchedEffect(state.messages) {
		val isScrolledToTheBottom = chatBubbleListState.layoutInfo.let {
			it.visibleItemsInfo.lastOrNull()?.index == state.messages.size - 1
		}

		if (isScrolledToTheBottom) {
			coroutineScope.launch {
				chatBubbleListState.scrollToItem(if (state.messages.size != 0) state.messages.size - 1 else 0)
			}
		}
	}

	Box(modifier) {
		ChatBubbleList(
			state = chatBubbleListState,
			messages = state.messages,
			modifier = Modifier.fillMaxSize()
		)
	}
}