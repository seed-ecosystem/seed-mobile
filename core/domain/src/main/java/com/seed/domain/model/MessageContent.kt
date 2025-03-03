package com.seed.domain.model

import com.seed.domain.values.ServerNonce

sealed interface MessageContent {
	val nonce: ServerNonce
	val receiveTimestamp: Long // TODO: This is a temporary logic

	data class RegularMessage(
		override val nonce: ServerNonce,
		val title: String,
		val text: String,
		override val receiveTimestamp: Long,
	) : MessageContent

	data class UnknownMessage(
		override val nonce: ServerNonce,
		override val receiveTimestamp: Long,
	) : MessageContent
}
