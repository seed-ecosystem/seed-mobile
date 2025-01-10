package com.seed.crypto.helpers

import com.seed.crypto.util.IV_SIZE
import com.seed.crypto.util.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCBlockSizeAES128
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.posix.size_tVar
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

actual object AesHelper {

	@OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
	actual fun decode(
		encryptedBase64: String,
		base64Iv: String,
		base64Key: String
	): Result<String> = runCatching {
		val encryptedData: NSData = Base64.decode(encryptedBase64).toNSData()
		val ivData: NSData = Base64.decode(base64Iv).toNSData()
		val keyData: NSData = Base64.decode(base64Key).toNSData()

		val decryptedBytes = ByteArray(encryptedData.length.toInt())
		val decryptedLength = memScoped {
			val numBytesDecrypted = alloc<size_tVar>()
			val result = CCCrypt(
				op = kCCDecrypt,
				alg = kCCAlgorithmAES,
				options = kCCOptionPKCS7Padding,
				key = keyData.bytes,
				keyLength = keyData.length,
				iv = ivData.bytes,
				dataIn = encryptedData.bytes,
				dataInLength = encryptedData.length,
				dataOut = decryptedBytes.refTo(0),
				dataOutAvailable = decryptedBytes.size.toULong(),
				dataOutMoved = numBytesDecrypted.ptr
			)

			if (result != kCCSuccess) {
				error("Decryption failed with status: $result")
			}
			numBytesDecrypted.value.toInt()
		}
		decryptedBytes.copyOf(decryptedLength).decodeToString()
	}

	@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
	actual fun encode(
		plainText: String,
		base64Key: String
	): Result<EncryptResult> = runCatching {

		val keyData = Base64.decode(base64Key).toNSData()

		// Generate IV
		val ivBytes: ByteArray = ByteArray(IV_SIZE).apply { Random.nextBytes(this) }
		val ivData = ivBytes.toNSData()

		val plainTextData = plainText.encodeToByteArray()
		val plainTextNSData = plainTextData.toNSData()

		val encryptedBytes = ByteArray(plainTextData.size + kCCBlockSizeAES128.toInt())

		val encryptedLength = memScoped {
			val numBytesEncrypted = alloc<size_tVar>()

			val result = CCCrypt(
				op = kCCEncrypt,
				alg = kCCAlgorithmAES,
				options = kCCOptionPKCS7Padding,
				key = keyData.bytes,
				keyLength = keyData.length,
				iv = ivBytes.refTo(0),
				dataIn = plainTextNSData.bytes,
				dataInLength = plainTextNSData.length,
				dataOut = encryptedBytes.refTo(0),
				dataOutAvailable = encryptedBytes.size.toULong(),
				dataOutMoved = numBytesEncrypted.ptr
			)

			if (result != kCCSuccess) {
				error("Encryption failed with status: $result")
			}

			numBytesEncrypted.value.toInt()
		}
		val ivBase64 = ivData.base64EncodedStringWithOptions(0u)
		val encryptedDataBase64 = encryptedBytes.copyOf(encryptedLength)
			.toNSData()
			.base64EncodedStringWithOptions(0u)
		EncryptResult(
			iv = ivBase64,
			encryptedContent = encryptedDataBase64
		)
	}
}