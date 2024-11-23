package com.seed.persistence.di

import androidx.room.Room
import com.seed.persistence.SeedDatabase
import com.seed.persistence.dao.ChatDao
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
}