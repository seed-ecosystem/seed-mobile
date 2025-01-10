package com.seed.shared

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.seed.shared.presentation.chat.logic.ChatScreenViewModel
import com.seed.shared.presentation.chat.ui.ChatScreen
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
	val context = LocalContext.current

	LaunchedEffect(Unit) {
		vm.setInitialData(
			chatName = initialData.chatName,
			chatId = initialData.chatId,
		)

		vm.loadData(
			onWaitEvent = {
				coroutineScope.launch {
					chatBubbleListState.scrollToItem(index = 0)
				}
			},
			onNewMessage = {

			}
		)
	}

	ChatScreen(
		coroutineScope = coroutineScope,
		onBackClick = onBackClick,
		onSend = {
			vm.sendMessage(
				onSuccess = {
					coroutineScope.launch {
						chatBubbleListState.animateScrollToItem(0)
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
