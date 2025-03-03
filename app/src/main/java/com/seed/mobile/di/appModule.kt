package com.seed.mobile.di

import com.seed.api.SeedApi
import com.seed.api.SeedEngine
import com.seed.api.util.SeedSocket
import com.seed.crypto.SeedCoder
import com.seed.data.ChatKeyRepositoryImpl
import com.seed.data.ChatRepositoryImpl
import com.seed.data.ChatsRepositoryImpl
import com.seed.data.SettingsRepositoryImpl
import com.seed.domain.KeyManager
import com.seed.domain.Logger
import com.seed.domain.SeedEngine
import com.seed.domain.SeedWorker
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.api.SeedApi
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.data.SettingsRepository
import com.seed.domain.usecase.AddChatUseCase
import com.seed.domain.usecase.CreateChatUseCase
import com.seed.domain.usecase.GetChatUrlUseCase
import com.seed.domain.usecase.GetMessageKeyUseCase
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import com.seed.domain.values.ServerUrl
import com.seed.mobile.LoggerImpl
import org.koin.dsl.module

val appModule = module {
	single { SubscribeToChatUseCase(get(), get(), get()) }
	single { SendMessageUseCase(get(), get(), get(), get(), get(), get(), get(), nonceAttempts = 50) }
	factory { GetMessageKeyUseCase(get(), get()) }
	factory { AddChatUseCase(get(), get(), get()) }
	factory { CreateChatUseCase(get(), get(), get()) }
	factory { GetChatUrlUseCase(get(), get(), get()) }

	single<KeyManager> { KeyManager(get(), get(), get()) }

	single<ChatRepository> { ChatRepositoryImpl(get(), get()) }
	factory<ChatsRepository> { ChatsRepositoryImpl(get(), get()) }
	factory<ChatKeyRepository> { ChatKeyRepositoryImpl(get()) }
	factory<SettingsRepository> { SettingsRepositoryImpl(get(), get()) }

	factory<SeedCoder> { SeedCoder(get()) }

	single<SeedSocket> {
		SeedSocket(
			logger = get(),
			reconnectionIntervalMillis = 1000L,
		)
	}

	single<SeedEngine> {
		SeedEngine(
			socket = get(),
			settingsRepository = get(),
			chatsRepository = get(),
			defaultMainServerUrl = ServerUrl("wss://api.meetacy.app/seed-kt"),
			pingIntervalMillis = 15000L,
		)
	}

	single<SeedApi> {
		SeedApi(
			logger = get(),
			engine = get(),
		)
	}

	single<SeedWorker> {
		SeedWorker(get(), get(), get(), get(), get(), get())
	}

	single<SeedWorkerStateHandle> {
		SeedWorkerStateHandle(get(), get(), get(), get())
	}

	single<Logger> { LoggerImpl() }
}
