package com.seed.persistence.dbo

import androidx.room.ColumnInfo
import androidx.room.Entity

enum class ChatEventType {
	NewMessage,
}

@Entity(primaryKeys = [ "chatId", "nonce" ])
data class ChatEventDbo(
	@ColumnInfo("chatId")
	val chatId: String,
	@ColumnInfo("nonce")
	val nonce: Int,
	val eventType: ChatEventType,
	val title: String,
	val text: String,
)
