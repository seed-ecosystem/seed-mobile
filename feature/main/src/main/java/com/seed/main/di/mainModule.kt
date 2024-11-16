package com.seed.main.di

import com.seed.main.presentation.chatlist.logic.ChatListScreenViewModel
import com.seed.main.presentation.chat.logic.ChatScreenViewModel
import com.seed.main.presentation.chatimport.logic.ChatImportScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
	viewModelOf(::ChatListScreenViewModel)
	viewModelOf(::ChatScreenViewModel)
	viewModelOf(::ChatImportScreenViewModel)
}

