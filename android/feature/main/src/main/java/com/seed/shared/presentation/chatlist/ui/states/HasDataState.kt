package com.seed.shared.presentation.chatlist.ui.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.seed.shared.presentation.chatlist.logic.ChatListItem
import com.seed.shared.presentation.chatlist.logic.ChatListScreenUiState
import com.seed.shared.presentation.chatlist.ui.components.ChatList

@Composable
fun HasDataState(
	onChatClick: (ChatListItem) -> Unit,
	state: ChatListScreenUiState.HasData,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
	) {
		ChatList(
			onChatClick = onChatClick,
			chats = state.chats,
			modifier = Modifier
				.fillMaxSize()
		)
	}
}