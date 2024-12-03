package com.seed.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.seed.main.ChatImportRoute
import com.seed.main.ChatListRoute
import com.seed.main.ChatRoute
import com.seed.main.ChatScreenInitialData
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
				Text(text = "settings dest")
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