package com.seed.main.presentation.chatcreate.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seed.main.presentation.chatcreate.ServerOption
import com.seed.main.presentation.chatcreate.logic.ChatCreateUiState
import com.seed.main.presentation.chatcreate.ui.states.IdleState
import com.seed.main.presentation.chatcreate.ui.states.LoadingState

@Composable
fun ChatCreateScreen(
	state: ChatCreateUiState,
	serverOptions: List<ServerOption>,
	onChatNameUpdate: (String) -> Unit,
	onServerUrlUpdate: (ServerOption) -> Unit,
	onChatCreate: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Scaffold(
		contentWindowInsets = WindowInsets(0.dp),
		modifier = modifier
	) { innerPadding ->
		val commonModifier = Modifier
			.fillMaxSize()
			.padding(innerPadding)

		when (state) {
			is ChatCreateUiState.Idle -> {
				IdleState(
					state = state,
					serverOptions = serverOptions,
					onChatNameUpdate = onChatNameUpdate,
					onServerUrlUpdate = onServerUrlUpdate,
					onChatCreate = onChatCreate,
					modifier = commonModifier,
				)
			}

			is ChatCreateUiState.Loading -> {
				LoadingState(
					modifier = commonModifier,
				)
			}
		}
	}
}
