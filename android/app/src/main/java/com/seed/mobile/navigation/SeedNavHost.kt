package com.seed.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.seed.shared.ChatImportRoute
import com.seed.shared.ChatListRoute
import com.seed.shared.ChatRoute
import com.seed.shared.ChatScreenInitialData
import com.seed.settings.SettingsRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun SeedNavHost(
	navHostController: NavHostController,
	modifier: Modifier = Modifier
) {
	NavHost(
		navController = navHostController,
		startDestination = NavDestination.BottomBarDestinations,
		modifier = modifier
	) {
		val commonModifier = Modifier
			.fillMaxSize()

		navigation<NavDestination.BottomBarDestinations>(
			startDestination = NavDestination.ChatListDestination
		) {
			composable<NavDestination.ChatListDestination> {
				ChatListRoute(
					goToChatImport = { navHostController.navigate(NavDestination.ChatImportDestination) },
					goToChat = { navHostController.navigate(NavDestination.ChatDestination(chatId = it.chatId, chatName = it.chatName)) },
					vm = koinViewModel(),
					modifier = commonModifier
				)
			}

			composable<NavDestination.SettingsDestination> {
				SettingsRoute(
					vm = koinViewModel(),
					modifier = commonModifier
				)
			}
		}

		composable<NavDestination.ChatDestination> { backStackEntry ->
			val destination = backStackEntry.toRoute<NavDestination.ChatDestination>()

			ChatRoute(
				initialData = ChatScreenInitialData(
					chatId = destination.chatId,
					chatName = destination.chatName,
				),
				onBackClick = {
					navHostController.popBackStack()
				},
				vm = koinViewModel(),
				modifier = commonModifier
			)
		}

		composable<NavDestination.ChatImportDestination> {
			ChatImportRoute(
				goToChatList = {
					navHostController.navigate(NavDestination.ChatListDestination)
				},
				vm = koinViewModel(),
				modifier = commonModifier
			)
		}
	}
}