package com.seed.crypto.helpers


expect class AesDecodingHelper() {
	fun decode(
		encryptedBase64: String,
		base64Iv: String,
		base64Key: String
	): Result<String>
}