package com.seed.settings.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	nickname: String,
	onNicknameChange: (String) -> Unit,
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

			Spacer(Modifier.height(8.dp))

			OutlinedTextField(
				value = nickname,
				onValueChange = onNicknameChange,
				placeholder = {
					Text(
						text = "Nickname"
					)
				},
				modifier = Modifier
					.fillMaxWidth()
			)

			Spacer(Modifier.height(8.dp))

			Text(
				text = "You can choose any nickname you want – there is no rules currently",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.secondary,
			)
		}
	}
}