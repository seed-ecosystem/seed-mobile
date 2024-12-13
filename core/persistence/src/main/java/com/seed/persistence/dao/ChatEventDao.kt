package com.seed.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seed.persistence.dbo.ChatEventDbo

@Dao
interface ChatEventDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insert(message: ChatEventDbo)

	@Query("SELECT * FROM ChatEventDbo WHERE chatId = :chatId ORDER BY nonce")
	suspend fun getAllByChatId(chatId: String): List<ChatEventDbo>

	@Query("SELECT * FROM ChatEventDbo")
	suspend fun getAll(): List<ChatEventDbo>
}