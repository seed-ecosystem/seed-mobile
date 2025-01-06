package com.seed.crypto.helpers

data class EncryptResult(
	val iv: String,
	val encryptedContent: String,
)

expect object AesHelper {
	fun decode(
		encryptedBase64: String,
		base64Iv: String,
		base64Key: String
	): Result<String>

	fun encode(
		plainText: String,
		base64Key: String
	): Result<EncryptResult>
}