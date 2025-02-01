package com.seed.settings.presentation.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seed.settings.presentation.logic.ServerOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	nickname: String,
	onNicknameChange: (String) -> Unit,
	onServerOptionChoice: (ServerOption) -> Unit,
	serverOptions: List<ServerOption>,
	selectedServer: ServerOption,
	modifier: Modifier = Modifier
) {
	Scaffold(
		topBar = {
			CenterAlignedTopAppBar(title = { Text("Settings") })
		},
		modifier = modifier,
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(horizontal = 32.dp)
				.padding(innerPadding)
				.fillMaxSize()
		) {
			Text(
				text = "Display nickname",
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.fillMaxWidth()
			)

			Spacer(Modifier.height(16.dp))

			OutlinedTextField(
				value = nickname,
				onValueChange = onNicknameChange,
				placeholder = {
					Text(
						text = "Nickname"
					)
				},
				supportingText = {
					Text(
						text = "You can choose any nickname you want – there is no rules currently",
					)
				},
				modifier = Modifier
					.fillMaxWidth()
			)

			Spacer(Modifier.height(16.dp))

			Text(
				text = "Main server",
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.fillMaxWidth()
			)

			Spacer(Modifier.height(8.dp))

			MainServerUrlOptions(
				onOptionChoice = onServerOptionChoice,
				serverOptions = serverOptions,
				selectedServer = selectedServer,
				modifier = Modifier
					.fillMaxWidth()
			)

			Spacer(Modifier.height(8.dp))

			Text(
				text = "The main server will serve all requests, even for the chat stored on the other server. All requests to the other server will be forwarded through the main server.",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.secondary,
			)
		}
	}
}

@Composable
private fun MainServerUrlOptions(
	onOptionChoice: (ServerOption) -> Unit,
	selectedServer: ServerOption,
	serverOptions: List<ServerOption>,
	modifier: Modifier = Modifier
) {
	LazyColumn(modifier) {
		items(serverOptions) { option ->
			SettingsOption(
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
private fun SettingsOption(
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
