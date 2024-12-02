package com.seed.domain.model

sealed interface MessageContent {
	data class RegularMessage(
		val nonce: Int,
		val author: String,
		val text: String,
	) : MessageContent

	data object UnknownMessage : MessageContent
}