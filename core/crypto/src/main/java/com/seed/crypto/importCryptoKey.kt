package com.seed.crypto

import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

// Import a cryptographic key
fun importCryptoKey(base64Key: String, algorithm: String): SecretKey {
	val decodedKey = Base64.getDecoder().decode(base64Key)
	return SecretKeySpec(decodedKey, algorithm)
}