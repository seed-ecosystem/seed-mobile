package com.seed.main.presentation.chatlist.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.seed.main.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreenTopBar(
	onImportChatClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	CenterAlignedTopAppBar(
		navigationIcon = {},
		windowInsets = WindowInsets(0.dp),
		actions = {
			TextButton(
				onClick = onImportChatClick
			) {
				Text(
					text = stringResource(R.string.import_chat)
				)
			}
		},
		title = {
			Text(text = "test")
		},
		modifier = modifier
	)
}
