package com.seed.crypto

data class DecodeOptions(
	val content: String,
	val contentIv: String,
	val signature: String,
	val key: String,
)

data class EncodeOptions(
	val content: MessageContent,
	val key: String,
)

class MessageContent(
	val plainText: String
)

data class EncodeResult(
	val content: String,
	val contentIV: String,
	val signature: String
)


interface SeedCoder {
	suspend fun decode(
		options: DecodeOptions
	): MessageContent?

	suspend fun encode(
		options: EncodeOptions
	): EncodeResult?

	suspend fun deriveNextKey(key: String): String
}