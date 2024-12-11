package com.seed.main.presentation.chat.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.seed.domain.api.SocketConnectionState
import com.seed.uikit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenTopBar(
	onBackClick: () -> Unit,
	chatName: String,
	connectionState: SocketConnectionState,
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
			TopBarTitle(chatName, connectionState)
		},
		windowInsets = WindowInsets(0.dp),
		modifier = modifier
	)
}

@Composable
private fun TopBarTitle(
	chatName: String,
	connectionState: SocketConnectionState,
	modifier: Modifier = Modifier,
) {
	val connectionStateString = when (connectionState) {
		SocketConnectionState.CONNECTED -> ""

		SocketConnectionState.RECONNECTING -> stringResource(R.string.reconnecting_top_bar_title)

		SocketConnectionState.DISCONNECTED -> stringResource(R.string.disconnected_top_bar_title)
	}

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier,
	) {
		Text(
			text = "$chatName $connectionStateString",
			modifier = Modifier
		)

		if (connectionState == SocketConnectionState.RECONNECTING || connectionState == SocketConnectionState.DISCONNECTED) {
			Spacer(Modifier.widthIn(16.dp))

			CircularProgressIndicator(
				color = MaterialTheme.colorScheme.onBackground,
				strokeWidth = 3.dp,
				modifier = Modifier
					.size(16.dp)
			)
		}
	}
}