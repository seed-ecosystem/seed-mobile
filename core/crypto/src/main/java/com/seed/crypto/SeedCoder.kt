package com.seed.crypto

import com.seed.crypto.helpers.AesDecodingHelper
import com.seed.crypto.helpers.AesEncodingHelper
import com.seed.crypto.helpers.HmacHelper
import com.seed.crypto.serial.DecryptedMessageContent
import com.seed.domain.Logger
import com.seed.domain.crypto.ChatUpdateDecodeResult
import com.seed.domain.crypto.MessageEncodeResult
import com.seed.domain.crypto.SeedCoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal data class EncodeResult(
	val content: String,
	val contentIv: String,
	val signature: String
)

fun SeedCoder(logger: Logger): SeedCoder = object : SeedCoder {
	val hmacHelper = HmacHelper()
	val decodingHelper = AesDecodingHelper()
	val encodingHelper = AesEncodingHelper()

	val json = Json { ignoreUnknownKeys = true }

	override suspend fun decodeChatUpdate(
		content: String,
		contentIv: String,
		signature: String,
		key: String
	): ChatUpdateDecodeResult? = withContext(Dispatchers.Default) {
		try {
			val decodeResult = decodingHelper.decode(
				encryptedBase64 = content,
				base64Iv = contentIv,
				base64Key = key
			)

			val decryptedMessageContent = decodeResult.getOrNull()?.let {
				try {
					json.decodeFromString<DecryptedMessageContent>(it)
				} catch (ex: Exception) {
					null
				}
			}

			val verify = hmacHelper.verifyHmacSha256(
				data = "SIGNATURE:" + decodeResult.getOrNull(),
				base64Key = key,
				base64Signature = signature
			)

			if (!verify) {
				logger.e(tag = "SeedCoder", message = "HMAC verification failed")
				return@withContext null
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

	override suspend fun encodeMessage(
		chatId: String,
		title: String,
		text: String,
		previousKey: String
	): MessageEncodeResult? = withContext(Dispatchers.Default) {
		val key = deriveNextKey(previousKey)
		val decryptedContent = DecryptedMessageContent(
			type = "regular",
			title = title,
			text = text
		)
		val decryptedContentJson = Json.encodeToString(decryptedContent)

		val encodeResult = encode(
			content = decryptedContentJson,
			key = key
		)

		return@withContext encodeResult?.let {
			MessageEncodeResult(
				key = key,
				signature = it.signature,
				content = it.content,
				contentIv = it.contentIv
			)
		}
	}

	private fun encode(
		content: String,
		key: String,
	): EncodeResult? {
		val signature = hmacHelper.hmacSha256(
			data = "SIGNATURE:$content",
			base64Key = key,
		)

		val encryptedContent = encodingHelper.encrypt(
			plainText = content,
			base64Key = key
		)

		return encryptedContent.getOrNull()?.let {
			EncodeResult(
				content = it.encryptedContent,
				contentIv = it.iv,
				signature = signature
			)
		}
	}

	override fun deriveNextKey(key: String): String {
		return hmacHelper.hmacSha256(
			data = "NEXT-KEY",
			base64Key = key
		)
	}
}
