package com.seed.persistence.db.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatDbo(
	@PrimaryKey
	val chatId: String,
	val chatKey: String,
	val firstChatKeyNonce: Int,
	val chatName: String,
	val serverUrl: String,
)
