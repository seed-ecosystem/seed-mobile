package com.seed.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.seed.persistence.dbo.ChatDbo
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
	@Insert
	fun insert(chatDbo: ChatDbo)

	@Query("SELECT * FROM chatdbo")
	fun getAll(): Flow<List<ChatDbo>>

	@Query("SELECT * FROM chatdbo WHERE chatId = :chatId")
	fun getById(chatId: String): ChatDbo?

	@Query("DELETE FROM chatdbo WHERE chatId = :chatId")
	fun deleteById(chatId: String)
}