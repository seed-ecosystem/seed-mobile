package com.seed.main.presentation.chatlist.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seed.domain.values.ChatId
import com.seed.main.presentation.chatlist.logic.ChatState
import com.seed.main.presentation.chatlist.logic.LastSentMessage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
	onClick: (ChatState) -> Unit,
	onLongClick: (ChatState) -> Unit,
	chat: ChatState,
	modifier: Modifier = Modifier
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.combinedClickable(
				onClick = { onClick(chat) },
				onLongClick = { onLongClick(chat) }
			)
			.padding(horizontal = 8.dp, vertical = 4.dp)
	) {
		Box(
			modifier = Modifier
				.clip(CircleShape)
				.size(52.dp)
				.background(Color.LightGray)
		)

		Spacer(Modifier.width(8.dp))

		Column(
			modifier = Modifier
				.weight(1f)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(
					text = chat.name,
					fontWeight = FontWeight.Bold,
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
					style = MaterialTheme.typography.titleMedium,
				)

//				Spacer(Modifier.weight(1f))
//
//				Text(
//					text = chat.lastSentMessage?.receiveTimestamp?.toString() ?: "",
//					fontWeight = FontWeight.Light,
//					color = MaterialTheme.colorScheme.secondary,
//					style = MaterialTheme.typography.labelLarge
//				)
			}

			Spacer(Modifier.height(1.dp))

			val author = chat.lastSentMessage?.author?.plus(":") ?: ""
			val text =
				"$author ${chat.lastSentMessage?.text ?: "no messages yet"}".trimIndent() // todo

			Text(
				text = text,
				overflow = TextOverflow.Ellipsis,
				maxLines = 2,
			)
		}

		if (chat.unreadCount > 0) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.primaryContainer)
					.size(24.dp)
			) {
				val unreadCountText = if(chat.unreadCount < 100) chat.unreadCount.toString() else "∞"

				Text(
					text = unreadCountText,
					fontSize = 16.sp,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
				)
			}
		}
	}
}

@Preview
@Composable
fun ChatListItemPreview() {
	ChatListItem(
		onClick = {},
		onLongClick = {},
		chat = ChatState(
			chatId = ChatId(""),
			name = "Some awesome group",
			lastSentMessage = LastSentMessage(
				"Demn",
				"Some last message text too long long lorem ipsum dolor. some last message text too long long lorem ipsum dolor. some last message text too long long lorem",
				receiveTimestamp = System.currentTimeMillis()
			),
			unreadCount = 2
		),
		modifier = Modifier.fillMaxWidth()
	)
}
