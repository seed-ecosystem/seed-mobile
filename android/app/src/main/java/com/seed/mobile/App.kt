package com.seed.mobile

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seed.mobile.navigation.SeedNavHost

@Composable
fun App(modifier: Modifier = Modifier) {
	val navHostController = rememberNavController()
	val navBackStackEntry by navHostController.currentBackStackEntryAsState()
	val currentDestination = navBackStackEntry?.destination

	Scaffold(
		bottomBar = {
			if (checkIfBottomBarVisible(currentDestination)) {
				SeedBottomBar(
					currentDestination = currentDestination,
					onDestinationClick = { navHostController.navigate(it) },
					destinationItems = bottomBarItems,
					modifier = Modifier
				)
			}
		},
		modifier = modifier
	) { innerPadding ->
		SeedNavHost(
			navHostController = navHostController,
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.consumeWindowInsets(innerPadding)
		)
	}
}