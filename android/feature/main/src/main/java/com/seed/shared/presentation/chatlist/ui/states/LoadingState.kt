package com.seed.shared.presentation.chatlist.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier,
	) {
		CircularProgressIndicator(
			Modifier.size(48.dp)
		)
	}
}