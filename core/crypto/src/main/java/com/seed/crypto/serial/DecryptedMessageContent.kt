package com.seed.crypto.serial

import kotlinx.serialization.Serializable

@Serializable
data class DecryptedMessageContent(
	val title: String,
	val text: String,
)
