package com.seed.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.seed.main.presentation.chatimport.logic.ChatImportScreenViewModel
import com.seed.main.presentation.chatimport.ui.ChatImportScreen

@Composable
fun ChatImportRoute(
	goToChatList: () -> Unit,
	vm: ChatImportScreenViewModel,
	modifier: Modifier = Modifier
) {
	val state by vm.state.collectAsState()
	val context = LocalContext.current

	ChatImportScreen(
		state = state,
		onKeyValueUpdate = vm::updateKeyValue,
		onProceed = {
			vm.proceedWithKey(
				onSuccess = goToChatList,
				onFailure = {
					Toast.makeText(
						context,
						"Failed to proceed with this string",
						Toast.LENGTH_SHORT
					).show()
				}
			)
		},
		modifier = modifier,
	)
}
