package com.seed.crypto.util

import java.security.SecureRandom

const val GCM_TAG_LENGTH: Int = 128 // in bytes
const val AES_KEY_SIZE: Int = 256 // in bytes

const val AES_ALGORITHM = "AES/GCM/NoPadding"
const val HMAC_ALGORITHM = "HmacSHA256"
const val IV_SIZE = 12

val secureRandom = SecureRandom()