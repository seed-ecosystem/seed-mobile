package com.seed.shared.presentation.chatlist.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seed.shared.presentation.chatlist.logic.ChatListItem
import com.seed.shared.presentation.chatlist.logic.ChatListScreenUiState
import com.seed.shared.presentation.chatlist.logic.generateRandomChats
import com.seed.shared.presentation.chatlist.ui.components.ChatListScreenTopBar
import com.seed.shared.presentation.chatlist.ui.states.ErrorState
import com.seed.shared.presentation.chatlist.ui.states.HasDataState
import com.seed.shared.presentation.chatlist.ui.states.LoadingState
import com.seed.shared.presentation.chatlist.ui.states.NoChatsState

@Composable
fun ChatListScreen(
	state: ChatListScreenUiState,
	onChatClick: (ChatListItem) -> Unit,
	onChatAddClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Scaffold(
		contentWindowInsets = WindowInsets(0.dp),
		floatingActionButton = {
			FloatingActionButton(onClick = onChatAddClick) {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = null
				)
			}
		},
		topBar = {
			ChatListScreenTopBar(
				modifier = Modifier
			)
		},
		modifier = modifier
	) { innerPadding ->
		val commonModifier = Modifier
			.fillMaxSize()
			.padding(innerPadding)

		when (state) {
			is ChatListScreenUiState.HasData -> {
				HasDataState(onChatClick, state, commonModifier)
			}

			is ChatListScreenUiState.Loading -> {
				LoadingState(commonModifier)
			}

			is ChatListScreenUiState.Error -> {
				ErrorState(commonModifier)
			}

			is ChatListScreenUiState.NoChats -> {
				NoChatsState(commonModifier)
			}
		}
	}
}

@Preview
@Composable
private fun ChatListScreenPreview() {
	ChatListScreen(
		state = ChatListScreenUiState.HasData(
			generateRandomChats()
		),
		onChatClick = {},
		onChatAddClick = {},
		modifier = Modifier
			.fillMaxSize()
	)
}