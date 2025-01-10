package com.seed.shared.presentation.chatimport.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ErrorState(modifier: Modifier) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier,
	) {
		Text(
			text = "an error occured"
		)
	}
}
