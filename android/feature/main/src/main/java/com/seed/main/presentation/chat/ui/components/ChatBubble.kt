package com.seed.main.presentation.chat.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.seed.main.presentation.chat.logic.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatBubble(
	message: Message,
	modifier: Modifier = Modifier
) {
	val cardColors = when (message) {
		is Message.SelfMessage -> CardDefaults.cardColors()

		else -> CardDefaults.outlinedCardColors()
	}

	Row(modifier) {
		if (message is Message.SelfMessage && message.isSending) {
			CircularProgressIndicator(
				strokeWidth = 4.dp,
				modifier = Modifier.size(16.dp),
			)

			Spacer(Modifier.width(8.dp))
		} else if (message is Message.SelfMessage && message.isSendFailed) {
			Icon(
				imageVector = Icons.Default.Warning,
				tint = MaterialTheme.colorScheme.error,
				contentDescription = null,
			)

			Spacer(Modifier.width(8.dp))
		}

		Column(
			modifier = Modifier
				.width(IntrinsicSize.Max)
				.widthIn(100.dp, 350.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				if (message is Message.OthersMessage) {
					Column(
						verticalArrangement = Arrangement.Bottom,
						modifier = Modifier
							.fillMaxHeight()
					) {
						TextAvatar(
							text = message.authorName,
						)
					}

					Spacer(Modifier.width(4.dp))
				}

				Card(
					colors = cardColors,
					modifier = Modifier
						.borderByAuthorType(message)
				) {
					Column(
						modifier = Modifier
							.padding(8.dp)
					) {
						if (message is Message.OthersMessage) {
							Text(
								text = message.authorName,
								style = MaterialTheme.typography.titleSmall,
								fontWeight = FontWeight.Bold,
								modifier = Modifier
									.wrapContentWidth()
							)
						}

						Spacer(Modifier.height(4.dp))

						Text(
							text = message.messageText,
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onBackground
						)

						Row(
							horizontalArrangement = Arrangement.End,
							modifier = Modifier
								.fillMaxWidth()
						) {
							Spacer(Modifier.weight(1f))
							Text(
								text = message.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
								style = MaterialTheme.typography.labelMedium,
								fontWeight = FontWeight.Light,
								color = MaterialTheme.colorScheme.secondary,
								modifier = Modifier
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun Modifier.borderByAuthorType(message: Message): Modifier {
	return when (message) {
		is Message.SelfMessage -> this

		else -> this.then(
			Modifier.border(
				border = CardDefaults.outlinedCardBorder(),
				shape = CardDefaults.outlinedShape
			)
		)
	}
}

@Preview
@Composable
private fun ChatBubblePreviewOthers() {
	ChatBubble(
		message = Message.OthersMessage(
			nonce = 220,
			authorName = "Andrew",
			messageText = LoremIpsum(2).values.first(),
			dateTime = LocalDateTime.now()
		),
	)
}

@Preview
@Composable
private fun ChatBubblePreviewSelf() {
	ChatBubble(
		message = Message.SelfMessage(
			nonce = 1,
			authorName = "я",
			messageText = "одногруппник работает на первой линии в b2b инфосек-конторе, иногда отвечает на тикеты прямо на парах. это милейшее зрелище, должен сказать: два взрослых человека на зарплате вежливо и предельно культурно обсуждают технические проблемы.",
			dateTime = LocalDateTime.now()
		),
	)
}