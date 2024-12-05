package com.seed.persistence.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatKeyDbo(
	@PrimaryKey
	val key: String,
	val chatId: String,
	val nonce: Int,
)