package com.seed.crypto.helpers

import com.seed.crypto.importCryptoKey
import com.seed.crypto.util.HMAC_ALGORITHM
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac

actual class HmacHelper {
	actual fun hmacSha256(data: String, base64Key: String): String {
		val key = importCryptoKey(base64Key, "HmacSHA256")
		val mac = Mac.getInstance(HMAC_ALGORITHM).apply {
			init(key)
		}
		val signature = mac.doFinal(data.toByteArray())
		return Base64.getEncoder().encodeToString(signature)
	}

	actual fun verifyHmacSha256(data: String, base64Key: String, base64Signature: String): Boolean {
		val key = importCryptoKey(base64Key, "HmacSHA256")
		val mac = Mac.getInstance(HMAC_ALGORITHM).apply {
			init(key)
		}
		val expectedSignature = mac.doFinal(data.toByteArray())
		val providedSignature = Base64.getDecoder().decode(base64Signature)
		return MessageDigest.isEqual(expectedSignature, providedSignature)
	}
}