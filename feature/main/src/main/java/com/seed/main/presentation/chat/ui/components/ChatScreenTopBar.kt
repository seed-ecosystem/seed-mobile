package com.seed.main.presentation.chat.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenTopBar(
	onBackClick: () -> Unit,
	chatName: String,
	modifier: Modifier = Modifier
) {
	CenterAlignedTopAppBar(
		navigationIcon = {
			IconButton(
				onClick = onBackClick
			) {
				Icon(
					imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
					contentDescription = null
				)
			}
		},
		title = {
			Text(text = chatName)
		},
		windowInsets = WindowInsets(0.dp),
		modifier = modifier
	)
}