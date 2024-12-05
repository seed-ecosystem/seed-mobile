package com.seed.main

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.seed.main.presentation.chat.logic.ChatScreenUiState
import com.seed.main.presentation.chat.logic.ChatScreenViewModel
import com.seed.main.presentation.chat.ui.ChatScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatScreenInitialData(
	val chatId: String,
	val chatName: String,
)

@Composable
fun ChatRoute(
	initialData: ChatScreenInitialData,
	onBackClick: () -> Unit,
	vm: ChatScreenViewModel,
	modifier: Modifier = Modifier
) {
	val state by vm.state.collectAsState()
	val chatBubbleListState = rememberLazyListState()
	val coroutineScope = rememberCoroutineScope()

	LaunchedEffect(Unit) {
		vm.setInitialData(
			chatName = initialData.chatName,
			chatId = initialData.chatId,
		)

		vm.loadData(
			onWaitEvent = {
				coroutineScope.launch {
					if (state is ChatScreenUiState.HasData) {
						while (chatBubbleListState.canScrollForward) {
							chatBubbleListState.animateScrollBy(100000f)
							delay(100)
						}
					}
				}
			},
			onNewMessage = {
				coroutineScope.launch {
					if (state is ChatScreenUiState.HasData) {
						while (chatBubbleListState.canScrollForward) {
							chatBubbleListState.animateScrollBy(100000f)
							delay(100)
						}
					}
				}
			}
		)
	}

	ChatScreen(
		onBackClick = onBackClick,
		onSend = {
			vm.sendMessage(
				onSuccess = {
					if (state is ChatScreenUiState.HasData) {
						coroutineScope.launch {
							chatBubbleListState.animateScrollToItem(
								(state as ChatScreenUiState.HasData).messages.size
							)
						}
					}
				},
				onFailure = {}
			)
		},
		onInputValueUpdate = vm::updateInputValue,
		state = state,
		chatBubbleListState = chatBubbleListState,
		modifier = modifier
	)
}