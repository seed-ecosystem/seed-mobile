package com.seed.mobile.di

import com.seed.api.createSeedMessagingApi
import com.seed.crypto.createSeedCoder
import com.seed.data.ChatRepositoryImpl
import com.seed.data.ChatsRepositoryImpl
import com.seed.domain.Logger
import com.seed.domain.api.SeedMessagingApi
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.usecase.GetChatHistoryUseCase
import com.seed.domain.usecase.GetMessageKeyUseCase
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import com.seed.mobile.LoggerImpl
import org.koin.dsl.module

val appModule = module {
	factory<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }

	factory { SubscribeToChatUseCase(get(), get(), get()) }
	factory { SendMessageUseCase(get(), get(), get(), get()) }
	factory { GetChatHistoryUseCase(get(), get(), get()) }
	factory { GetMessageKeyUseCase(get(), get()) }

	single<ChatsRepository> { ChatsRepositoryImpl(get(), get()) }
	factory<SeedCoder> { createSeedCoder() }

	single<SeedMessagingApi> {
		createSeedMessagingApi(
			logger = get(),
			host = "api.meetacy.app",
			path = "seed-go",
		)
	}

	single<Logger> { LoggerImpl() }
}