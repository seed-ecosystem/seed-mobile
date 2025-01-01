package com.seed.mobile.di

import com.seed.api.SeedApi
import com.seed.api.util.SeedSocket
import com.seed.crypto.SeedCoder
import com.seed.data.ChatKeyRepositoryImpl
import com.seed.data.ChatRepositoryImpl
import com.seed.data.ChatsRepositoryImpl
import com.seed.data.NicknameRepositoryImpl
import com.seed.domain.Logger
import com.seed.domain.SeedWorker
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.api.SeedApi
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatKeyRepository
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.data.NicknameRepository
import com.seed.domain.usecase.AddChatUseCase
import com.seed.domain.usecase.GetMessageKeyUseCase
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import com.seed.mobile.LoggerImpl
import org.koin.dsl.module

val appModule = module {
	single { SubscribeToChatUseCase(get(), get(), get()) }
	single { SendMessageUseCase(get(), get(), get(), get(), get()) }
	factory { GetMessageKeyUseCase(get(), get()) }
	factory { AddChatUseCase(get(),get()) }

	single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }
	factory<ChatsRepository> { ChatsRepositoryImpl(get(), get(), get()) }
	factory<ChatKeyRepository> { ChatKeyRepositoryImpl(get()) }
	factory<NicknameRepository> { NicknameRepositoryImpl(get()) }

	factory<SeedCoder> { SeedCoder(get()) }

	single<SeedSocket> {
		SeedSocket(
			logger = get(),
			host = "api.meetacy.app",
			path = "seed-go",
			reconnectionIntervalMillis = 1000L,
		)
	}

	single<SeedApi> {
		SeedApi(
			logger = get(),
			socket = get(),
		)
	}

	single<SeedWorker> {
		SeedWorker(get(), get(), get(), get(), get())
	}

	single<SeedWorkerStateHandle> {
		SeedWorkerStateHandle(get(), get(), get())
	}

	single<Logger> { LoggerImpl() }
}
