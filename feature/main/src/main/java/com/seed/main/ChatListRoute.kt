package com.seed.main

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import com.seed.main.presentation.chatlist.logic.ChatListItem
import com.seed.main.presentation.chatlist.logic.ChatListScreenViewModel
import com.seed.main.presentation.chatlist.ui.ChatListScreen
import kotlinx.coroutines.launch

@Composable
fun ChatListRoute(
	goToChatImport: () -> Unit,
	goToAddChat: () -> Unit,
	goToChat: (ChatListItem) -> Unit,
	vm: ChatListScreenViewModel,
	modifier: Modifier = Modifier,
) {
	val state by vm.state.collectAsState()
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	LaunchedEffect(Unit) {
		vm.loadData()
	}

	ChatListScreen(
		state = state,
		onChatClick = goToChat,
		onLongChatClick = { chat ->
			scope.launch {
				val url = vm.getChatShareUrl(chat.chatId)
				share(context, url ?: "no")
			}
		},
		onChatAddClick = goToAddChat,
		onImportChatClick = goToChatImport,
		modifier = modifier
	)
}

fun share(context: Context, content: String) {
	val sendIntent: Intent = Intent().apply {
		action = Intent.ACTION_SEND
		putExtra(Intent.EXTRA_TEXT, content)
		type = "text/plain"
	}

	val shareIntent = Intent.createChooser(sendIntent, null)

	context.startActivity(shareIntent)
}
