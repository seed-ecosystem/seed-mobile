package com.seed.main.di

import com.seed.main.presentation.chat.logic.ChatScreenViewModel
import com.seed.main.presentation.chatcreate.logic.ChatCreateScreenViewModel
import com.seed.main.presentation.chatimport.logic.ChatImportScreenViewModel
import com.seed.main.presentation.chatlist.logic.ChatListScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
	viewModelOf(::ChatListScreenViewModel)
//	viewModel<ChatScreenViewModel> { (options: ChatScreenViewModelOptions) -> ChatScreenViewModel(options, get(), get(), get(), get(), get()) }
	viewModelOf(::ChatScreenViewModel)
	viewModelOf(::ChatImportScreenViewModel)
	viewModelOf(::ChatCreateScreenViewModel)
}

