package com.seed.main.presentation.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seed.main.presentation.chat.logic.AuthorType
import com.seed.main.presentation.chat.logic.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatBubble(
	message: Message,
	modifier: Modifier = Modifier
) {
	val cardColors = when (message.authorType) {
		AuthorType.Self -> CardDefaults.cardColors()

		else -> CardDefaults.outlinedCardColors()
	}

	Column(
		modifier = modifier
			.widthIn(50.dp, 300.dp)
//			.height(IntrinsicSize.Max) // todo
	) {
		Row {
			if (message.authorType == AuthorType.Others) {
				Column(
					verticalArrangement = Arrangement.Bottom,
					modifier = Modifier
						.fillMaxHeight()
				) {
					Box(
						modifier = Modifier
							.size(48.dp)
							.background(Color.LightGray, CircleShape)
					)
				}

				Spacer(Modifier.width(4.dp))
			}

			Card(
				colors = cardColors,
				modifier = Modifier
					.borderByAuthorType(message.authorType)
			) {
				Column(
					modifier = Modifier
						.padding(8.dp)
				) {
					if (message.authorType == AuthorType.Others) {
						Text(
							text = message.authorName,
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
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

@Composable
private fun Modifier.borderByAuthorType(authorType: AuthorType): Modifier {
	return when (authorType) {
		AuthorType.Self -> this

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
		message = Message(
			nonce = 220,
			authorType = AuthorType.Others,
			authorName = "Andrew",
			messageText = "Some message text.. idk",
			dateTime = LocalDateTime.now()
		),
	)
}

@Preview
@Composable
private fun ChatBubblePreviewSelf() {
	ChatBubble(
		message = Message(
			nonce = 1,
			authorType = AuthorType.Self,
			authorName = "я",
			messageText = "одногруппник работает на первой линии в b2b инфосек-конторе, иногда отвечает на тикеты прямо на парах. это милейшее зрелище, должен сказать: два взрослых человека на зарплате вежливо и предельно культурно обсуждают технические проблемы.",
			dateTime = LocalDateTime.now()
		),
	)
}