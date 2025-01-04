package com.seed.crypto.helpers

data class EncryptResult(
	val iv: String,
	val encryptedContent: String,
)

expect class AesEncodingHelper() {
	fun encrypt(
		plainText: String,
		base64Key: String
	): Result<EncryptResult>
}
