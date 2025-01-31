package com.seed.main.presentation.chat.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seed.main.presentation.chat.logic.Message

@Composable
fun ChatBubbleList(
	state: LazyListState,
	messages: List<Message>,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
	) {
		LazyColumn(
			state = state,
			reverseLayout = true,
			modifier = Modifier
		) {
//			stickyHeader {
//				Row(
//					horizontalArrangement = Arrangement.Center,
//					modifier = Modifier
//						.fillMaxWidth()
//						.padding(vertical = 8.dp)
//				) {
//					Text(
//						text = "11.16",
//						color = MaterialTheme.colorScheme.secondary,
//						style = MaterialTheme.typography.labelMedium
//					)
//				}
//			}

			items(messages, key = { it.localNonce }) { message ->
				Spacer(Modifier.height(8.dp))

				Row(
					horizontalArrangement = horizontalArrangementByAuthorType(message),
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 8.dp)
						.animateItem(
							fadeInSpec = null,
							fadeOutSpec = null
						)
				) {
					ChatBubble(
						message = message
					)
				}
			}
		}
	}
}

@Composable
private fun horizontalArrangementByAuthorType(message: Message) =
	if (message is Message.SelfMessage) Arrangement.End else Arrangement.Start
