package com.seed.data

import com.seed.domain.data.SettingsRepository
import com.seed.persistence.pref.MainServerSharedPreferences
import com.seed.persistence.pref.NicknameSharedPreferences

class SettingsRepositoryImpl(
	private val nicknameSharedPreferences: NicknameSharedPreferences,
	private val mainServerSharedPreferences: MainServerSharedPreferences,
) : SettingsRepository {
	override fun setNickname(nickname: String) {
		nicknameSharedPreferences.setNickname(nickname)
	}

	override fun getNickname(): String? {
		return nicknameSharedPreferences.getNickname()
	}

	override fun setMainServerUrl(url: String) {
		mainServerSharedPreferences.setMainServerUrl(url)
	}

	override fun getMainServerUrl(): String? {
		return mainServerSharedPreferences.getMainServerUrl()
	}
}
