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
	fun getAll(): Flow<ChatDbo>
}