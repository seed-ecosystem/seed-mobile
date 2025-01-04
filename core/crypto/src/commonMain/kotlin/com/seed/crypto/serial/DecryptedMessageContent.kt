package com.seed.crypto.serial

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DecryptedMessageContent(
	@SerialName("type")
	val type: String,
	@SerialName("title")
	val title: String,
	@SerialName("text")
	val text: String,
)
