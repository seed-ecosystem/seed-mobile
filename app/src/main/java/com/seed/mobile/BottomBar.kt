package com.seed.mobile

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.seed.mobile.navigation.NavDestination

data class BottomBarItem(
	val destination: NavDestination.BottomBarDestination,
	val iconResourceId: Int,
	val titleResourceId: Int,
)

val bottomBarItems = listOf(
	BottomBarItem(
		destination = NavDestination.ChatListDestination,
		iconResourceId = R.drawable.ic_forum_outined,
		titleResourceId = R.string.chats_bottom_bar_title
	),
	BottomBarItem(
		destination = NavDestination.SettingsDestination,
		iconResourceId = R.drawable.ic_settings_outlined,
		titleResourceId = R.string.settings_bottom_bar_title
	),
)

@Composable
fun SeedBottomBar(
	currentDestination: androidx.navigation.NavDestination?,
	onDestinationClick: (NavDestination.BottomBarDestination) -> Unit,
	destinationItems: List<BottomBarItem>,
	modifier: Modifier = Modifier
) {
	NavigationBar(modifier = modifier) {
		for (item in destinationItems) {
			NavigationBarItem(
				selected = checkIfBottomBarItemSelected(currentDestination, item),
				onClick = { onDestinationClick(item.destination) },
				label = {
					Text(text = stringResource(item.titleResourceId))
				},
				icon = {
					Icon(
						painter = painterResource(item.iconResourceId),
						contentDescription = null
					)
				}
			)
		}
	}
}

private fun checkIfBottomBarItemSelected(
	currentDestination: androidx.navigation.NavDestination?,
	item: BottomBarItem
) = currentDestination?.hierarchy?.any { it.hasRoute(item.destination::class) } ?: false

fun checkIfBottomBarVisible(currentDestination: androidx.navigation.NavDestination?): Boolean =
	currentDestination?.hierarchy?.any { hierarchyItem ->
		bottomBarItems.any { bottomBarItem ->
			hierarchyItem.hasRoute(bottomBarItem.destination::class)
		}
	} ?: false