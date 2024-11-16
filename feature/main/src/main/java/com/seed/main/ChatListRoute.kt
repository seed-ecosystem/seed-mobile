package com.seed.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.seed.main.presentation.chatlist.logic.ChatListItem
import com.seed.main.presentation.chatlist.logic.ChatListScreenViewModel
import com.seed.main.presentation.chatlist.ui.ChatListScreen

@Composable
fun ChatListRoute(
	goToChatImport: () -> Unit,
	goToChat: (ChatListItem) -> Unit,
	vm: ChatListScreenViewModel,
	modifier: Modifier = Modifier
) {
	val state by vm.state.collectAsState()

	LaunchedEffect(Unit) {
		vm.loadData()
	}

	ChatListScreen(
		state = state,
		onChatClick = goToChat,
		onChatAddClick = goToChatImport,
		modifier = modifier
	)
}