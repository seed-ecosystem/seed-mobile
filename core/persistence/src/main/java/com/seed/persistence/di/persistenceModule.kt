package com.seed.persistence.di

import androidx.room.Room
import com.seed.persistence.db.SeedDatabase
import com.seed.persistence.db.dao.ChatDao
import com.seed.persistence.db.dao.ChatKeyDao
import com.seed.persistence.db.dao.ChatEventDao
import com.seed.persistence.pref.MainServerSharedPreferences
import com.seed.persistence.pref.NicknameSharedPreferences
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
		database.getChatEventDao()
	}

	factory<NicknameSharedPreferences> {
		NicknameSharedPreferences(get())
	}

	factory<MainServerSharedPreferences> {
		MainServerSharedPreferences(get())
	}
}
