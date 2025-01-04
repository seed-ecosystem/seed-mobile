package com.seed.crypto.helpers

import com.seed.crypto.importCryptoKey
import com.seed.crypto.util.AES_ALGORITHM
import com.seed.crypto.util.GCM_TAG_LENGTH
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

actual class AesDecodingHelper {
	actual fun decode(
		encryptedBase64: String,
		base64Iv: String,
		base64Key: String
	): Result<String> {
		val key = importCryptoKey(base64Key, "AES")
		val iv = Base64.getDecoder().decode(base64Iv)

		val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
			init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
		}
		val decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64))
		return Result.success(String(decryptedData))
	}
}