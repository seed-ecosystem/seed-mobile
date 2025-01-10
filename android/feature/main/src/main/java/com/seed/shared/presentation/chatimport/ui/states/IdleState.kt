package com.seed.shared.presentation.chatimport.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seed.main.R
import com.seed.shared.presentation.chatimport.logic.ChatImportScreenUiState

@Composable
fun IdleState(
	onKeyValueUpdate: (String) -> Unit,
	onProceed: () -> Unit,
	state: ChatImportScreenUiState.Idle,
	modifier: Modifier = Modifier
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Icon(
				painter = painterResource(com.seed.uikit.R.drawable.ic_key_outlined),
				contentDescription = null,
				modifier = Modifier.size(96.dp)
			)

			Spacer(Modifier.height(16.dp))

			Text(
				text = stringResource(R.string.enter_chat_key),
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold
			)

			Spacer(Modifier.height(16.dp))

			OutlinedTextField(
				value = state.keyValue,
				onValueChange = onKeyValueUpdate,
			)

			Spacer(Modifier.height(16.dp))

			Column(Modifier.height(40.dp)) {
				if (state.keyValue.isNotBlank()) {
					Button(
						onClick = onProceed,
						colors = ButtonDefaults.filledTonalButtonColors()
					) {
						Text(
							text = stringResource(R.string.proceed),
							modifier = Modifier
						)
					}
				}
			}
		}
	}
}