package com.seed.mobile.navigation

import kotlinx.serialization.Serializable

interface NavDestination {
	interface BottomBarDestination : NavDestination

	@Serializable
	data object ChatListDestination : BottomBarDestination

	@Serializable
	data object SettingsDestination : BottomBarDestination

	@Serializable
	data object BottomBarDestinations : BottomBarDestination

	@Serializable
	data class ChatDestination(val chatId: String = "", val chatName: String = "") : NavDestination

	@Serializable
	data object ChatImportDestination : NavDestination
}