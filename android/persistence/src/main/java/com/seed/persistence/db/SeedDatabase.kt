package com.seed.persistence.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seed.persistence.db.dao.ChatDao
import com.seed.persistence.db.dao.ChatKeyDao
import com.seed.persistence.db.dao.ChatEventDao
import com.seed.persistence.db.dbo.ChatDbo
import com.seed.persistence.db.dbo.ChatEventDbo
import com.seed.persistence.db.dbo.ChatKeyDbo

@Database(entities = [ChatDbo::class, ChatKeyDbo::class, ChatEventDbo::class], version = 1)
abstract class SeedDatabase : RoomDatabase() {
	companion object {
		const val DATABASE_NAME = "SeedDB"
	}

	abstract fun getChatDao(): ChatDao

	abstract fun getChatKeyDao(): ChatKeyDao

	abstract fun getChatEventDao(): ChatEventDao
}