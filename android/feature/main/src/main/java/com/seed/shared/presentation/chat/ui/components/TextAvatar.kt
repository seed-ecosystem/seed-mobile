package com.seed.shared.presentation.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextAvatar(
	text: String,
	modifier: Modifier = Modifier
) {
	val splitText = text.split(' ')
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.size(48.dp)
			.clip(CircleShape)
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		val calculatedLetters = calculateLetters(splitText)
		Text(
			text = calculatedLetters,
			fontSize = calculateFontSize(calculatedLetters.length),
			color = MaterialTheme.colorScheme.secondary,
			modifier = Modifier
		)
	}
}

fun calculateFontSize(count: Int): TextUnit {
	return when (count) {
		1 -> 32.sp

		2 -> 24.sp

		else -> 20.sp
	}
}

private fun calculateLetters(text: List<String>): String {
	return when (text.size) {
		1 -> {
			if (text[0].isNotEmpty()) {
				findFirstLetter(text[0]) ?: text[0].first().toString()
			} else ""
		}

		2 -> {
			val first = findFirstLetter(text[0]) ?: text[0].first().toString()
			val second =  findFirstLetter(text[1]) ?: ""
			"$first$second"
		}

		3 -> {
			val first = findFirstLetter(text[0]) ?: text[0].first().toString()
			val second =  findFirstLetter(text[1]) ?: ""
			val third =  findFirstLetter(text[2]) ?: ""

			"$first$second$third"
		}

		else -> ""
	}.uppercase()
}

fun findFirstLetter(input: String): String? {
	val regex = "[\\p{IsCyrillic}\\p{IsLatin}]".toRegex()

	val matchResult = regex.find(input)

	return matchResult?.value?.first()?.toString()
}

@Preview
@Composable
private fun TextAvatarPreview_SingleWord() {
	TextAvatar(
		text = "Demn"
	)
}

@Preview
@Composable
private fun TextAvatarPreview_TwoWords() {
	TextAvatar(
		text = "Alex Sokol"
	)
}

@Preview
@Composable
private fun TextAvatarPreview_ThreeWords() {
	TextAvatar(
		text = "bosnia and herzegovina"
	)
}