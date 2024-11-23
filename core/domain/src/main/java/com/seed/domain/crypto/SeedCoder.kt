package com.seed.domain.crypto

import com.seed.domain.model.MessageContent

data class DecodeOptions(
	val content: String,
	val contentIv: String,
	val signature: String,
	val key: String,
)

data class EncodeOptions(
	val content: String,
	val key: String,
)

data class EncodeResult(
	val content: String,
	val contentIv: String,
	val signature: String
)

data class MessageEncodeOptions(
	val chatId: String,
	val title: String,
	val text: String,
	val previousKey: String,
)

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
		options: DecodeOptions
	): ChatUpdateDecodeResult?

	suspend fun encodeMessage(
		options: MessageEncodeOptions,
	): MessageEncodeResult?

	suspend fun decode(
		options: DecodeOptions
	): String?

	suspend fun encode(
		options: EncodeOptions
	): EncodeResult?

	suspend fun deriveNextKey(key: String): String
}