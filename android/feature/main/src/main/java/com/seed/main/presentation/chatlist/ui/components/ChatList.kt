package com.seed.main.presentation.chatlist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seed.main.presentation.chatlist.logic.ChatListItem

@Composable
fun ChatList(
	onChatClick: (ChatListItem) -> Unit,
	chats: List<ChatListItem>,
	modifier: Modifier = Modifier
) {
	LazyColumn(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		item {
			Spacer(Modifier.height(8.dp))
		}

		items(chats) { chat ->
			ChatListItem(
				onClick = onChatClick,
				chat = chat,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}