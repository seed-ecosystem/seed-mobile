package com.seed.domain.model

sealed interface MessageContent {
	val nonce: Int

	data class RegularMessage(
		override val nonce: Int,
		val title: String,
		val text: String,
	) : MessageContent

	data class UnknownMessage(override val nonce: Int) : MessageContent
}