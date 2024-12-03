package com.seed.crypto

import com.seed.crypto.helpers.AesDecodingHelper
import com.seed.crypto.helpers.AesEncodingHelper
import com.seed.crypto.helpers.HmacHelper
import com.seed.crypto.serial.DecryptedMessageContent
import com.seed.domain.crypto.ChatUpdateDecodeResult
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.EncodeOptions
import com.seed.domain.crypto.EncodeResult
import com.seed.domain.crypto.MessageEncodeOptions
import com.seed.domain.crypto.MessageEncodeResult
import com.seed.domain.crypto.SeedCoder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun createSeedCoder(): SeedCoder = object : SeedCoder {
	val hmacHelper = HmacHelper()
	val decodingHelper = AesDecodingHelper()
	val encodingHelper = AesEncodingHelper()

	val json = Json { ignoreUnknownKeys = true }

	override suspend fun decodeChatUpdate(options: DecodeOptions): ChatUpdateDecodeResult? {
		val decodeResult = decodingHelper.decode(
			encryptedBase64 = options.content,
			base64Iv = options.contentIv,
			base64Key = options.key
		)

		val decryptedMessageContent = decodeResult.getOrNull()?.let {
			json.decodeFromString<DecryptedMessageContent>(it)
		}

		return decryptedMessageContent?.let {
			ChatUpdateDecodeResult(
				title = it.title,
				text = it.text
			)
		}
	}

	override suspend fun encodeMessage(options: MessageEncodeOptions): MessageEncodeResult? {
		val key = deriveNextKey(options.previousKey)
		val decryptedContent = DecryptedMessageContent(
//			type = "regular",
			title = options.title,
			text = options.text
		)
		val decryptedContentJson = Json.encodeToString(decryptedContent)

		val encodeOptions = EncodeOptions(
			content = decryptedContentJson,
			key = key
		)

		val encodeResult = encode(encodeOptions)

		return encodeResult?.let {
			MessageEncodeResult(
				key = key,
				signature = it.signature,
				content = it.content,
				contentIv = it.contentIv
			)
		}
	}

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