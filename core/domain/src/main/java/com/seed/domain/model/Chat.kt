package com.seed.domain.model

import com.seed.domain.values.ServerUrl

data class Chat(
	val chatId: String,
	val name: String,
	val firstChatKeyNonce: Int,
	val serverUrl: ServerUrl,
)
