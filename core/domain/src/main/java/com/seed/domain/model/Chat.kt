package com.seed.domain.model

import com.seed.domain.values.ChatId
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl

data class Chat(
	val chatId: ChatId,
	val name: String,
	val firstChatKeyNonce: ServerNonce,
	val serverUrl: ServerUrl,
)
