package com.seed.mobile.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
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
import com.seed.main.presentation.chat.logic.ChatScreenViewModelOptions
import com.seed.settings.SettingsRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SeedNavHost(
	navHostController: NavHostController,
	modifier: Modifier = Modifier
) {
	NavHost(
		navController = navHostController,
		startDestination = NavDestination.BottomBarDestinations,
		enterTransition = { EnterTransition.None },
		exitTransition = { ExitTransition.None },
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
					goToChat = {
						navHostController.navigate(
							NavDestination.ChatDestination(
								chatId = it.chatId,
								chatName = it.chatName
							)
						)
					},
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

		composable<NavDestination.ChatDestination>(
			enterTransition = {
				fadeIn(
					animationSpec = tween(
						100,
						easing = LinearEasing
					)
				) + slideInHorizontally(
					animationSpec = tween(
						100,
						easing = LinearEasing
					),
					initialOffsetX = { it / 4 }
				)
			},
			exitTransition = {
				fadeOut(
					animationSpec = tween(100, easing = LinearEasing)
				) + slideOutHorizontally(
					tween(
						100,
						easing = LinearEasing
					),
					targetOffsetX = { it / 8 }
				)
			},
		) { backStackEntry ->
			val destination = backStackEntry.toRoute<NavDestination.ChatDestination>()

			ChatRoute(
				initialData = ChatScreenInitialData(
					chatId = destination.chatId,
					chatName = destination.chatName,
				),
				onBackClick = {
					navHostController.popBackStack()
				},
				vm = koinViewModel {
					parametersOf(
						ChatScreenViewModelOptions(
							destination.chatName,
							destination.chatId
						)
					)
				},
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
