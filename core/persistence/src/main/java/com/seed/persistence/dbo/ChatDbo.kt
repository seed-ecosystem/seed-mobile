package com.seed.persistence.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatDbo(
	@PrimaryKey
	val chatId: String,
	val chatKey: String,
)
