package com.seed.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.seed.main.presentation.chatcreate.logic.ChatCreateScreenViewModel
import com.seed.main.presentation.chatcreate.serverList
import com.seed.main.presentation.chatcreate.ui.ChatCreateScreen

@Composable
fun ChatCreateScreenRoute(
	goBack: () -> Unit,
	vm: ChatCreateScreenViewModel,
	modifier: Modifier = Modifier
) {
	val state by vm.state.collectAsState()
	val context = LocalContext.current

	ChatCreateScreen(
		state = state,
		serverOptions = serverList,
		onChatNameUpdate = vm::updateChatName,
		onServerUrlUpdate = vm::updateServerUrl,
		onChatCreate = {
			vm.createChat(
				onSuccess = goBack,
				onFailure = {
					Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
				}
			)
		},
		modifier = modifier,
	)
}
