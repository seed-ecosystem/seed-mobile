package com.seed.domain.crypto

import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey

data class MessageEncodeResult(
	val key: ChatKey,
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
		key: ChatKey,
	): ChatUpdateDecodeResult?

	suspend fun encodeMessage(
		chatId: ChatId,
		title: String,
		text: String,
		previousKey: ChatKey,
	): MessageEncodeResult?

	suspend fun encodeMessageWithKey(
		chatId: ChatId,
		title: String,
		text: String,
		messageKey: ChatKey,
	): MessageEncodeResult?

	fun deriveNextKey(key: ChatKey): ChatKey
}
