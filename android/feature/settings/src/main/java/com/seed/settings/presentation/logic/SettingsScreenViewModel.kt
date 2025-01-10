package com.seed.settings.presentation.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seed.domain.data.NicknameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class SettingsScreenVmState(
	val nicknameValue: String = "",
) {
	fun toUiState(): SettingsScreenUiState {
		return SettingsScreenUiState(this.nicknameValue)
	}
}

class SettingsScreenViewModel(
	private val nicknameRepository: NicknameRepository,
) : ViewModel() {
	private val _state = MutableStateFlow(SettingsScreenVmState())

	val state: StateFlow<SettingsScreenUiState> = _state
		.map(SettingsScreenVmState::toUiState)
		.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsScreenUiState(""))

	fun loadData() {
		_state.update {
			it.copy(
				nicknameValue = nicknameRepository.getNickname() ?: ""
			)
		}
	}

	fun onNicknameChange(newValue: String) {
		_state.update {
			it.copy(
				nicknameValue = newValue
			)
		}

		nicknameRepository.setNickname(newValue.trim())
	}
}