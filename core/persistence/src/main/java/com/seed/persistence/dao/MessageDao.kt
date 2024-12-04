package com.seed.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.seed.persistence.dbo.MessageDbo
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
	@Insert
	suspend fun insert(message: MessageDbo)

	@Query("SELECT * FROM MessageDbo")
	fun getAll(): Flow<MessageDbo>
}