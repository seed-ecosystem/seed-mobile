package com.seed.main.presentation.chatlist.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreenTopBar(
	modifier: Modifier = Modifier
) {
	CenterAlignedTopAppBar(
		navigationIcon = {},
		windowInsets = WindowInsets(0.dp),
		actions = {},
		title = {
			Text(text = "test")
		},
		modifier = modifier
	)
}