package com.seed.crypto

import com.seed.crypto.helpers.AesDecodingHelper
import com.seed.crypto.helpers.AesEncodingHelper
import com.seed.crypto.helpers.HmacHelper
import com.seed.crypto.serial.DecryptedMessageContent
import com.seed.domain.Logger
import com.seed.domain.crypto.ChatUpdateDecodeResult
import com.seed.domain.crypto.DecodeOptions
import com.seed.domain.crypto.EncodeOptions
import com.seed.domain.crypto.EncodeResult
import com.seed.domain.crypto.MessageEncodeOptions
import com.seed.domain.crypto.MessageEncodeResult
import com.seed.domain.crypto.SeedCoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception

fun createSeedCoder(logger: Logger): SeedCoder = object : SeedCoder {
	val hmacHelper = HmacHelper()
	val decodingHelper = AesDecodingHelper()
	val encodingHelper = AesEncodingHelper()

	val json = Json { ignoreUnknownKeys = true }

	override suspend fun decodeChatUpdate(options: DecodeOptions): ChatUpdateDecodeResult? = withContext(Dispatchers.Default) {
		try {
			val decodeResult = decodingHelper.decode(
				encryptedBase64 = options.content,
				base64Iv = options.contentIv,
				base64Key = options.key
			)

			val decryptedMessageContent = decodeResult.getOrNull()?.let {
				try {
					json.decodeFromString<DecryptedMessageContent>(it)
				} catch (ex: Exception) {
					null
				}
			}

			return@withContext decryptedMessageContent?.let {
				ChatUpdateDecodeResult(
					title = it.title,
					text = it.text
				)
			}
		} catch (ex: Exception) {
			logger.e(tag = "SeedCoder", message = "${ex.message}")
			return@withContext  null
		}
	}

	override suspend fun encodeMessage(options: MessageEncodeOptions): MessageEncodeResult? = withContext(Dispatchers.Default) {
		val key = deriveNextKey(options.previousKey)
		val decryptedContent = DecryptedMessageContent(
			type = "regular",
			title = options.title,
			text = options.text
		)
		val decryptedContentJson = Json.encodeToString(decryptedContent)

		val encodeOptions = EncodeOptions(
			content = decryptedContentJson,
			key = key
		)

		val encodeResult = encode(encodeOptions)

		return@withContext encodeResult?.let {
			MessageEncodeResult(
				key = key,
				signature = it.signature,
				content = it.content,
				contentIv = it.contentIv
			)
		}
	}

	override suspend fun decode(options: DecodeOptions): String? = withContext(Dispatchers.Default) {
		val decryptedResult = decodingHelper.decode(
			encryptedBase64 = options.content,
			base64Iv = options.contentIv,
			base64Key = options.key
		)

		if (decryptedResult.isFailure) {
			return@withContext null
		}

		val verify = hmacHelper.verifyHmacSha256(
			data = "SIGNATURE" + decryptedResult.getOrNull(),
			base64Key = options.key,
			base64Signature = options.signature
		)

		if (!verify) {
			logger.e(tag = "SeedCoder", message = "HMAC verification failed")
			return@withContext null
		}

		return@withContext decryptedResult.getOrNull()
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

	override suspend fun deriveNextKey(key: String): String = withContext(Dispatchers.Default) {
		return@withContext hmacHelper.hmacSha256(
			data = "NEXT-KEY",
			base64Key = key
		)
	}
}