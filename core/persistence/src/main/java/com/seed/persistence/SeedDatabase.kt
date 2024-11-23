package com.seed.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seed.persistence.dao.ChatDao
import com.seed.persistence.dbo.ChatDbo

@Database(entities = [ChatDbo::class], version = 1)
abstract class SeedDatabase : RoomDatabase() {
	companion object {
		const val DATABASE_NAME = "SeedDB"
	}

	abstract fun getChatDao(): ChatDao
}