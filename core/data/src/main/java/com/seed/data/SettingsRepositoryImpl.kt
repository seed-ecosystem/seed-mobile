package com.seed.data

import com.seed.domain.data.SettingsRepository
import com.seed.domain.values.ServerUrl
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

	override fun setMainServerUrl(url: ServerUrl) {
		mainServerSharedPreferences.setMainServerUrl(url.value)
	}

	override fun getMainServerUrl(): ServerUrl? {
		return mainServerSharedPreferences.getMainServerUrl()?.let { ServerUrl(it) }
	}
}
