package com.seed.shared.di

import com.seed.shared.presentation.chatlist.logic.ChatListScreenViewModel
import com.seed.shared.presentation.chat.logic.ChatScreenViewModel
import com.seed.shared.presentation.chatimport.logic.ChatImportScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
	viewModelOf(::ChatListScreenViewModel)
	viewModelOf(::ChatScreenViewModel)
	viewModelOf(::ChatImportScreenViewModel)
}

