package com.seed.main.presentation.chatimport.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seed.main.presentation.chatimport.logic.ChatImportScreenUiState
import com.seed.main.presentation.chatimport.ui.states.ErrorState
import com.seed.main.presentation.chatimport.ui.states.IdleState
import com.seed.main.presentation.chatimport.ui.states.LoadingState

@Composable
fun ChatImportScreen(
	state: ChatImportScreenUiState,
	onKeyValueUpdate: (String) -> Unit,
	onProceed: () -> Unit,
	modifier: Modifier = Modifier
) {
	Scaffold(
		contentWindowInsets = WindowInsets(0.dp),
		modifier = modifier
	) { innerPadding ->
		val commonModifier = Modifier
			.fillMaxSize()
			.padding(innerPadding)

		when (state) {
			is ChatImportScreenUiState.Idle -> {
				IdleState(
					state = state,
					onKeyValueUpdate = onKeyValueUpdate,
					onProceed = onProceed,
					modifier = commonModifier
				)
			}

			is ChatImportScreenUiState.Loading -> {
				LoadingState(commonModifier)
			}

			is ChatImportScreenUiState.Error -> {
				ErrorState(commonModifier)
			}
		}
	}
}
