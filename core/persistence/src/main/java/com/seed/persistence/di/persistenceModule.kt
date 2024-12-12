package com.seed.persistence.di

import androidx.room.Room
import com.seed.persistence.SeedDatabase
import com.seed.persistence.dao.ChatDao
import com.seed.persistence.dao.ChatKeyDao
import com.seed.persistence.dao.ChatEventDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val persistenceModule = module {
	single<SeedDatabase> {
		Room.databaseBuilder(
			androidApplication(),
			SeedDatabase::class.java,
			SeedDatabase.DATABASE_NAME
		).build()
	}

	factory<ChatDao> {
		val database = get<SeedDatabase>()
		database.getChatDao()
	}

	factory<ChatKeyDao> {
		val database = get<SeedDatabase>()
		database.getChatKeyDao()
	}

	factory<ChatEventDao> {
		val database = get<SeedDatabase>()
		database.getMessageDao()
	}
}