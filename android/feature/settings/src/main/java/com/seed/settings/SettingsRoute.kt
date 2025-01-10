package com.seed.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.seed.settings.presentation.logic.SettingsScreenViewModel
import com.seed.settings.presentation.ui.SettingsScreen

@Composable
fun SettingsRoute(
	vm: SettingsScreenViewModel,
	modifier: Modifier = Modifier
) {
	val state by vm.state.collectAsState()

	LaunchedEffect(Unit) {
		vm.loadData()
	}

	SettingsScreen(
		nickname = state.nicknameValue,
		onNicknameChange = vm::onNicknameChange,
		modifier = modifier,
	)
}