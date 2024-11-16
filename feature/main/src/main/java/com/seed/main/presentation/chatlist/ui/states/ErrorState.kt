package com.seed.main.presentation.chatlist.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ErrorState(modifier: Modifier = Modifier) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier,
	) {
		Text("an error occured")
	}
}