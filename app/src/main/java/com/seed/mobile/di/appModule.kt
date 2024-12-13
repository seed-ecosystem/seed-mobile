package com.seed.mobile.di

import com.seed.api.createSeedMessagingApi
import com.seed.api.util.SeedSocket
import com.seed.api.util.createSeedSocket
import com.seed.crypto.createSeedCoder
import com.seed.data.ChatKeyRepositoryImpl
import com.seed.data.ChatRepositoryImpl
import com.seed.data.ChatsRepositoryImpl
import com.seed.domain.Logger
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.usecase.AddChatUseCase
import com.seed.domain.usecase.GetMessageKeyUseCase
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import com.seed.mobile.LoggerImpl
import org.koin.dsl.module

val appModule = module {
	single { SubscribeToChatUseCase(get(), get(), get(), get()) }
	single { SendMessageUseCase(get(), get(), get(), get()) }
	factory { GetMessageKeyUseCase(get(), get()) }
	factory { AddChatUseCase(get()) }

	single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }
	factory<ChatsRepository> { ChatsRepositoryImpl(get(), get(), get()) }
	factory<ChatKeyRepository> { ChatKeyRepositoryImpl(get()) }

	factory<SeedCoder> { createSeedCoder(get()) }

	single<SeedSocket> {
		createSeedSocket(
			logger = get(),
			host = "api.meetacy.app",
			path = "seed-go",
			reconnectionIntervalMillis = 1000L,
		)
	}

	single<SeedMessagingApi> {
		createSeedMessagingApi(
			logger = get(),
			socket = get(),
		)
	}

	single<Logger> { LoggerImpl() }
}