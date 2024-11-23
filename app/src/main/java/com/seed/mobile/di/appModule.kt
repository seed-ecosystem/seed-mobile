package com.seed.mobile.di

import com.seed.crypto.createSeedCoder
import com.seed.data.ChatRepositoryImpl
import com.seed.data.ChatsRepositoryImpl
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.usecase.SendMessageUseCase
import com.seed.domain.usecase.SubscribeToChatUseCase
import org.koin.dsl.module

val appModule = module {
	factory<ChatRepository> { ChatRepositoryImpl() }

	factory { SubscribeToChatUseCase(get(), get()) }
	factory { SendMessageUseCase(get(), get()) }

	single<ChatsRepository> { ChatsRepositoryImpl(get()) }
	factory<SeedCoder> { createSeedCoder() }
}