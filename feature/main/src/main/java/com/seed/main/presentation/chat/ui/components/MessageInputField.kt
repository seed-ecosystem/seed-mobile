package com.seed.main.presentation.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seed.main.R
import kotlinx.coroutines.launch

@Composable
fun MessageInputField(
	inputValue: String,
	onInputValueUpdate: (String) -> Unit,
	onSend: () -> Unit,
	modifier: Modifier = Modifier
) {
	val focusRequester = remember { FocusRequester() }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.background(
				color = MaterialTheme.colorScheme.background
			)
			.heightIn(24.dp, 128.dp)
			.padding(
				horizontal = 8.dp,
				vertical = 4.dp,
			)
	) {
		BasicTextField(
			value = inputValue,
			onValueChange = onInputValueUpdate,
			textStyle = MaterialTheme.typography.bodyLarge,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
			minLines = 1,
			maxLines = 5,
			modifier = modifier
				.weight(1f)
				.focusRequester(focusRequester)
		) { innerTextField ->
			Box {
				innerTextField()

				if (inputValue.isEmpty()) {
					Text(
						text = stringResource(R.string.message_field_placeholder),
						color = MaterialTheme.colorScheme.secondary,
						style = MaterialTheme.typography.bodyLarge,
					)
				}
			}
		}

		IconButton(
			onClick = onSend
		) {
			Icon(
				imageVector = Icons.AutoMirrored.Filled.Send,
				tint = MaterialTheme.colorScheme.primary,
				contentDescription = null
			)
		}
	}
}

@Preview
@Composable
private fun MessageInputFieldPreview() {
	var value by remember { mutableStateOf("") }

	MessageInputField(
		inputValue = value,
		onInputValueUpdate = { value = it },
		onSend = {},
		modifier = Modifier.fillMaxWidth()
	)
}