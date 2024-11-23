package com.seed.crypto

import com.seed.crypto.helpers.AesDecodingHelper
import com.seed.crypto.helpers.AesEncodingHelper
import com.seed.crypto.helpers.HmacHelper
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.EncodeOptions
import com.seed.domain.crypto.EncodeResult
import com.seed.domain.crypto.SeedCoder

fun createSeedCoder(): SeedCoder = object : SeedCoder {
	val hmacHelper = HmacHelper()
	val decodingHelper = AesDecodingHelper()
	val encodingHelper = AesEncodingHelper()

	override suspend fun decode(options: DecodeOptions): String? {
		val verify = hmacHelper.verifyHmacSha256(
			data = "SIGNATURE",
			base64Key = options.key,
			base64Signature = options.signature
		)

		if (!verify) {
			return null
		}

		val decryptedResult = decodingHelper.decode(
			encryptedBase64 = options.content,
			base64Iv = options.contentIv,
			base64Key = options.key
		)

		return decryptedResult.getOrNull()
	}

	override suspend fun encode(options: EncodeOptions): EncodeResult? {
		val signature = hmacHelper.hmacSha256(
			data = "SIGNATURE",
			base64Key = options.key,
		)

		val encryptedContent = encodingHelper.encrypt(
			plainText = options.content,
			base64Key = options.key
		)

		return encryptedContent.getOrNull()?.let {
			EncodeResult(
				content = it.encryptedContent,
				contentIv = it.iv,
				signature = signature
			)
		}
	}

	override suspend fun deriveNextKey(key: String): String {
		return hmacHelper.hmacSha256(
			data = "NEXT-KEY",
			base64Key = key
		)
	}
}