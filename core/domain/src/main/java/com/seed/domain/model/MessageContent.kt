package com.seed.domain.model

import com.seed.domain.values.ServerNonce

sealed interface MessageContent {
	val nonce: ServerNonce

	data class RegularMessage(
		override val nonce: ServerNonce,
		val title: String,
		val text: String,
	) : MessageContent

	data class UnknownMessage(override val nonce: ServerNonce) : MessageContent
}
