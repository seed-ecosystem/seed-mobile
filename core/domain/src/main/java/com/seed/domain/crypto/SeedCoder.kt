package com.seed.domain.crypto

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

interface SeedCoder {
	suspend fun decode(
		options: DecodeOptions
	): String?

	suspend fun encode(
		options: EncodeOptions
	): EncodeResult?

	suspend fun deriveNextKey(key: String): String
}