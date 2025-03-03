package com.seed.persistence.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seed.persistence.db.dbo.ChatEventDbo
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatEventDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insert(message: ChatEventDbo)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertAll(messages: List<ChatEventDbo>)

	@Query("SELECT * FROM ChatEventDbo WHERE chatId = :chatId ORDER BY nonce")
	suspend fun getAllByChatId(chatId: String): List<ChatEventDbo>

	@Query("SELECT * FROM ChatEventDbo")
	suspend fun getAll(): List<ChatEventDbo>

	@Query("SELECT * FROM ChatEventDbo")
	fun getAllFlow(): Flow<List<ChatEventDbo>>

	@Query("SELECT * FROM chateventdbo WHERE chatId = :chatId ORDER BY receiveTimestamp DESC LIMIT 1")
	suspend fun getLastByChatId(chatId: String): ChatEventDbo?
}
