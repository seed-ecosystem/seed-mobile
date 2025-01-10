package com.seed.settings.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.seed.settings.presentation.logic.SettingsScreenViewModel

val settingsModule = module {
	viewModelOf(::SettingsScreenViewModel)
}