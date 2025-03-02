package com.seed.main.presentation.chatcreate.ui.states

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seed.main.R
import com.seed.main.presentation.chatcreate.ServerOption
import com.seed.main.presentation.chatcreate.logic.ChatCreateUiState

@Composable
fun IdleState(
	state: ChatCreateUiState.Idle,
	serverOptions: List<ServerOption>,
	onChatNameUpdate: (String) -> Unit,
	onServerUrlUpdate: (ServerOption) -> Unit,
	onChatCreate: () -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier,
	) {
		Column(
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxSize()
				.align(Alignment.Center)
				.padding(horizontal = 32.dp)
		) {
			Text(
				text = stringResource(R.string.create_new_chat),
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold,
			)

			Spacer(Modifier.height(32.dp))

			OutlinedTextField(
				value = state.chatName,
				onValueChange = onChatNameUpdate,
				placeholder = { Text(text = stringResource(R.string.chat_name)) },
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(Modifier.height(8.dp))

			ServerUrlOptions(
				onOptionChoice = onServerUrlUpdate,
				selectedServer = state.chatServerUrl,
				serverOptions = serverOptions,
				modifier = Modifier
			)

			Spacer(Modifier.height(8.dp))

			Button(
				onClick = onChatCreate,
				enabled = state.isValid,
				modifier = Modifier
			) {
				Text(
					text = stringResource(R.string.create)
				)
			}
		}
	}
}

@Composable
private fun ServerUrlOptions(
	onOptionChoice: (ServerOption) -> Unit,
	selectedServer: ServerOption,
	serverOptions: List<ServerOption>,
	modifier: Modifier = Modifier
) {
	LazyColumn(modifier) {
		items(serverOptions) { option ->
			ServerOption(
				text = option.serverName,
				selected = option == selectedServer,
				onOptionChoice = { onOptionChoice(option) },
				modifier = Modifier
					.fillMaxWidth()
			)
		}
	}
}

@Composable
private fun ServerOption(
	text: String,
	selected: Boolean,
	onOptionChoice: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.clickable { onOptionChoice() }
			.padding(vertical = 8.dp)
	) {
		RadioButton(
			selected = selected,
			onClick = null,
			modifier = Modifier
		)

		Spacer(Modifier.width(8.dp))

		Text(
			text = text,
			modifier = Modifier
		)
	}
}
