package com.seed.crypto

import com.seed.crypto.util.AES_ALGORITHM_NAME
import com.seed.crypto.util.AES_KEY_SIZE
import com.seed.crypto.util.secureRandom
import java.util.Base64
import javax.crypto.KeyGenerator

actual fun randomAESKey(): String {
	val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM_NAME).apply {
		init(AES_KEY_SIZE, secureRandom)
	}
	val secretKey = keyGenerator.generateKey()
	return Base64.getEncoder().encodeToString(secretKey.encoded)
}

