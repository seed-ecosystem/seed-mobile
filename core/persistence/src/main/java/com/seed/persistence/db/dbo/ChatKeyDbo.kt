package com.seed.persistence.db.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatKeyDbo(
	val nonce: Int,
	@PrimaryKey
	val key: String,
	val chatId: String,
)
