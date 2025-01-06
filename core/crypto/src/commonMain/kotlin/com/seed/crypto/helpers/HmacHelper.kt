package com.seed.crypto.helpers

expect object HmacHelper {
	fun hmacSha256(data: String, base64Key: String): String
	fun verifyHmacSha256(data: String, base64Key: String, base64Signature: String): Boolean
}
