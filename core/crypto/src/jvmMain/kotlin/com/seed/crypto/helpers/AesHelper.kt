package com.seed.crypto.helpers

import com.seed.crypto.importCryptoKey
import com.seed.crypto.util.AES_ALGORITHM
import com.seed.crypto.util.AES_ALGORITHM_NAME
import com.seed.crypto.util.GCM_TAG_LENGTH
import com.seed.crypto.util.IV_SIZE
import com.seed.crypto.util.secureRandom
import java.security.GeneralSecurityException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

actual object AesHelper {
	actual fun decode(
		encryptedBase64: String,
		base64Iv: String,
		base64Key: String
	): Result<String> {
		val key = importCryptoKey(base64Key, AES_ALGORITHM_NAME)
		val iv = Base64.getDecoder().decode(base64Iv)

		val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
			init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
		}
		val decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64))
		return Result.success(String(decryptedData))
	}

	actual fun encode(
		plainText: String,
		base64Key: String
	): Result<EncryptResult> {
		try {
			val key = importCryptoKey(base64Key, AES_ALGORITHM_NAME)
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