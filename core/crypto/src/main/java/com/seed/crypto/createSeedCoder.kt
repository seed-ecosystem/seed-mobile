package com.seed.crypto

import com.seed.crypto.helpers.AesDecodingHelper
import com.seed.crypto.helpers.AesEncodingHelper
import com.seed.crypto.helpers.HmacHelper

fun createSeedCoder(): SeedCoder = object : SeedCoder {
	val hmacHelper = HmacHelper()
	val decodingHelper = AesDecodingHelper()
	val encodingHelper = AesEncodingHelper()

	override suspend fun decode(options: DecodeOptions): MessageContent? {
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

		return decryptedResult.getOrNull()?.let { MessageContent(it) }
	}

	override suspend fun encode(options: EncodeOptions): EncodeResult? {
		val signature = hmacHelper.hmacSha256(
			data = "SIGNATURE",
			base64Key = options.key,
		)

		val encryptedContent = encodingHelper.encrypt(
			plainText = options.content.plainText,
			base64Key = options.key
		)

		return encryptedContent.getOrNull()?.let {
			EncodeResult(
				content = it.encryptedContent,
				contentIV = it.iv,
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