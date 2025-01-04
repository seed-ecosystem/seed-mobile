package com.seed.crypto.helpers

import com.seed.crypto.util.AES_ALGORITHM
import com.seed.crypto.util.GCM_TAG_LENGTH
import com.seed.crypto.importCryptoKey
import com.seed.crypto.util.IV_SIZE
import com.seed.crypto.util.secureRandom
import java.security.GeneralSecurityException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

actual class AesEncodingHelper {
	actual fun encrypt(
		plainText: String,
		base64Key: String
	): Result<EncryptResult> {
		try {
			val key = importCryptoKey(base64Key, "AES")
			val iv = ByteArray(IV_SIZE).apply { secureRandom.nextBytes(this) }
			val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
				init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
			}
			val encryptedData = cipher.doFinal(plainText.toByteArray())

			val encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData)
			val ivBase64 = Base64.getEncoder().encodeToString(iv)

			return Result.success(
				EncryptResult(
					iv = ivBase64,
					encryptedContent = encryptedDataBase64
				)
			)
		} catch (e: GeneralSecurityException) {
			return Result.failure(e)
		} catch (e: Exception) {
			return Result.failure(e)
		}
	}
}
