package com.seed.persistence.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageDbo(
	@PrimaryKey
	val chatId: String,
	val nonce: Int,
	val title: String,
	val text: String,
)
