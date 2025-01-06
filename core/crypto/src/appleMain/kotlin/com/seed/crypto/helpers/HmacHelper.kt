package com.seed.crypto.helpers

import com.seed.crypto.util.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.Foundation.base64EncodedStringWithOptions
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual object HmacHelper {

	@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
	actual fun hmacSha256(data: String, base64Key: String): String {
		val keyData = Base64.decode(base64Key).toNSData()

		val dataToSign = data.encodeToByteArray().toNSData()
		val hmacOutput = ByteArray(CC_SHA256_DIGEST_LENGTH)

		CCHmac(
			algorithm = kCCHmacAlgSHA256,
			key = keyData.bytes,
			keyLength = keyData.length,
			data = dataToSign.bytes,
			dataLength = dataToSign.length,
			macOut = hmacOutput.refTo(0)
		)

		return hmacOutput.toNSData().base64EncodedStringWithOptions(0u)
	}

	actual fun verifyHmacSha256(data: String, base64Key: String, base64Signature: String): Boolean {
		val expectedSignature = hmacSha256(data, base64Key)
		return expectedSignature.contentEquals(base64Signature)
	}
}