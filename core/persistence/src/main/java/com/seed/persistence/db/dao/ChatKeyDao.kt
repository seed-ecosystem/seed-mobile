package com.seed.persistence.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seed.persistence.db.dbo.ChatKeyDbo

@Dao
interface ChatKeyDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun set(key: ChatKeyDbo)

	@Query("SELECT * FROM ChatKeyDbo WHERE chatId = :chatId AND nonce = :nonce")
	suspend fun getByNonce(chatId: String, nonce: Int): ChatKeyDbo?

	@Query("SELECT * FROM ChatKeyDbo WHERE chatId = :chatId ORDER BY nonce ASC LIMIT 1")
	suspend fun getOldest(chatId: String): ChatKeyDbo?

	@Query("SELECT * FROM ChatKeyDbo WHERE chatId = :chatId ORDER BY nonce DESC LIMIT 1")
	suspend fun getLatest(chatId: String): ChatKeyDbo?
}