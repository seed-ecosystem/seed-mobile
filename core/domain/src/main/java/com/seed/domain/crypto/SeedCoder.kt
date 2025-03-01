package com.seed.domain.crypto

import com.seed.domain.values.ChatId

data class MessageEncodeResult(
	val key: String,
	val signature: String,
	val content: String,
	val contentIv: String,
)

data class ChatUpdateDecodeResult(
	val title: String,
	val text: String,
)

interface SeedCoder {
	suspend fun decodeChatUpdate(
		content: String,
		contentIv: String,
		signature: String,
		key: String,
	): ChatUpdateDecodeResult?

	suspend fun encodeMessage(
		chatId: String,
		title: String,
		text: String,
		previousKey: String,
	): MessageEncodeResult?

	suspend fun encodeMessageWithKey(
		chatId: ChatId,
		title: String,
		text: String,
		messageKey: String
	): MessageEncodeResult?

	fun deriveNextKey(key: String): String
}
