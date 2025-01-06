package com.seed.crypto

import com.seed.crypto.util.AES_KEY_SIZE
import com.seed.crypto.util.toNSData
import platform.Foundation.base64EncodedStringWithOptions
import kotlin.random.Random

actual fun randomAESKey(): String {
	val keyBytes = ByteArray(AES_KEY_SIZE / 8).apply { Random.nextBytes(this) }
	return keyBytes.toNSData().base64EncodedStringWithOptions(0u)
}
