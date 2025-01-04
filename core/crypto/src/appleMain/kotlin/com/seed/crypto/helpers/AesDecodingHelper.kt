package com.seed.crypto.helpers

import com.seed.crypto.util.AES_ALGORITHM
import com.seed.crypto.util.GCM_TAG_LENGTH
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import platform.CoreCrypto.
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64EncodingEndLineWithCarriageReturn
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedDataWithOptions
import platform.Foundation.base64Encoding
import platform.Foundation.create
import platform.posix.size_tVar

actual class AesDecodingHelper {
	@OptIn(ExperimentalForeignApi::class)
	actual fun decode(
		encryptedBase64: String,
		base64Iv: String,
		base64Key: String
	): Result<String> = runCatching {
		val encryptedData: NSData = TODO() //NSData.create(encryptedBase64.decodeBase64())
		val ivData: NSData = TODO() //NSData.create(base64Iv.decodeBase64())
		val keyData: NSData = TODO() //NSData.create(base64Key.decodeBase64())

		requireNotNull(encryptedData) { "Invalid encrypted data" }
		requireNotNull(ivData) { "Invalid IV data" }
		requireNotNull(keyData) { "Invalid key data" }


		val decryptedBytes: ByteArray = TODO() //ByteArray(encryptedData.length.toInt())
		val decryptedLength = memScoped {
			val numBytesDecrypted = alloc<size_tVar>()
			val result = CCCrypt(
				op = kCCDecrypt,
				alg = kCCAlgorithmAES,
				options = kCCOptionPKCS7Padding,
				key = keyData.bytes,
				keyLength = keyData.length.toULong(),
				iv = ivData.bytes,
				dataIn = encryptedData.bytes,
				dataInLength = encryptedData.length.toULong(),
				dataOut = decryptedBytes.refTo(0),
				dataOutAvailable = decryptedBytes.size.toULong(),
				dataOutMoved = numBytesDecrypted.ptr
			)
		}
		""
	}

//	@OptIn(ExperimentalForeignApi::class)
//	private fun String.decodeBase64(): ByteArray? {
//		return NSData.create(this, NSUTF8StringEncoding)
//			?.base64EncodedDataWithOptions(NSDataBase64EncodingEndLineWithCarriageReturn)?.by()?.let { bytes ->
//			ByteArray(bytes.size) { i -> bytes[i].toInt().toByte() }
//		}
//	}
}