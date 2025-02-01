package com.seed.settings.presentation.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.data.SettingsRepository
import com.seed.settings.presentation.serverList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class SettingsScreenVmState(
	val nicknameValue: String = "",
	val selectedServer: ServerOption = ServerOption("", ""),
) {
	fun toUiState(): SettingsScreenUiState {
		return SettingsScreenUiState(
			nicknameValue = this.nicknameValue,
			selectedServer = this.selectedServer,
		)
	}
}

class SettingsScreenViewModel(
	private val settingsRepository: SettingsRepository,
) : ViewModel() {
	private val _state = MutableStateFlow(SettingsScreenVmState())

	val state: StateFlow<SettingsScreenUiState> = _state
		.map(SettingsScreenVmState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			SettingsScreenUiState("", ServerOption("", ""))
		)

	fun loadData() {
		val selectedServer: ServerOption? =
			settingsRepository.getMainServerUrl()?.let { storedMainServerUrl ->
				serverList.find { it.serverUrl == storedMainServerUrl }
			}

		_state.update {
			it.copy(
				nicknameValue = settingsRepository.getNickname() ?: "",
				selectedServer = selectedServer ?: serverList.first(),
			)
		}
	}

	fun onNicknameChange(newValue: String) {
		_state.update {
			it.copy(
				nicknameValue = newValue
			)
		}

		settingsRepository.setNickname(newValue.trim())
	}

	fun updateServerOption(new: ServerOption) {
		_state.update {
			it.copy(selectedServer = new)
		}

		settingsRepository.setMainServerUrl(new.serverUrl)
	}
}
