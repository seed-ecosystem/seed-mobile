package com.seed.api.models

import kotlinx.serialization.Serializable

@Serializable
data class SubscribeRequest(
	val type: String,
	val nonce: Int,
	val chatId: String,
)