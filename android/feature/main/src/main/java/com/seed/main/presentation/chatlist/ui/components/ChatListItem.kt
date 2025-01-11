package com.seed.main.presentation.chatlist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.seed.main.presentation.chatlist.logic.ChatListItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatListItem(
	onClick: (ChatListItem) -> Unit,
	chat: ChatListItem,
	modifier: Modifier = Modifier
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.clickable { onClick(chat) }
			.padding(horizontal = 8.dp)
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
					text = chat.chatName,
					fontWeight = FontWeight.Bold,
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
					style = MaterialTheme.typography.titleMedium,
				)

				Spacer(Modifier.weight(1f))

				Text(
					text = chat.lastSentMessageDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
					fontWeight = FontWeight.Light,
					color = MaterialTheme.colorScheme.secondary,
					style = MaterialTheme.typography.labelLarge
				)
			}

			Spacer(Modifier.height(1.dp))

			Text(
				text = chat.lastSentMessageText,
				overflow = TextOverflow.Ellipsis,
				maxLines = 2,
			)
		}
	}
}

@Preview
@Composable
fun ChatListItemPreview() {
	ChatListItem(
		onClick = {},
		chat = ChatListItem(
			chatId = "",
			chatName = "Some awesome group",
			lastSentMessageDateTime = LocalDateTime.now(),
			lastSentMessageText = "Some last message text too long long lorem ipsum dolor. some last message text too long long lorem ipsum dolor. some last message text too long long lorem"
		),
		modifier = Modifier.fillMaxWidth()
	)
}