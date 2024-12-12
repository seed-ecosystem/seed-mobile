package com.seed.main

import android.widget.Toast
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.seed.main.presentation.chat.logic.ChatScreenUiState
import com.seed.main.presentation.chat.logic.ChatScreenViewModel
import com.seed.main.presentation.chat.ui.ChatScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatScreenInitialData(
	val chatId: String,
	val chatName: String,
)

suspend fun LazyListState.animateScrollToItemWithBounce(
	index: Int,
	scrollOffset: Int = 0,
	animationSpec: AnimationSpec<Float> = spring(
		dampingRatio = Spring.DampingRatioLowBouncy,
		stiffness = Spring.StiffnessMediumLow
	)
) {
	// Combine the default animateScrollToItem with a custom spring animation
	animateScrollToItem(
		index = index,
		scrollOffset = scrollOffset,
		
	)
}

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
		coroutineScope.launch {
			vm.debugEvents.collect { debugEvent ->
				Toast.makeText(context, debugEvent, Toast.LENGTH_SHORT).show()
			}
		}

		vm.setInitialData(
			chatName = initialData.chatName,
			chatId = initialData.chatId,
		)

		vm.loadData(
			onWaitEvent = {
				coroutineScope.launch {
					if (state is ChatScreenUiState.HasData) {
						val hasDataState = state as ChatScreenUiState.HasData

						chatBubbleListState.scrollToItem(index = hasDataState.messages.size)
					}
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
					if (state is ChatScreenUiState.HasData) {
						coroutineScope.launch {
							chatBubbleListState.scrollToItem(
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