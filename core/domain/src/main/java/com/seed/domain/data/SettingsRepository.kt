package com.seed.domain.data

interface SettingsRepository {
	fun setNickname(nickname: String)

	fun getNickname(): String?

	fun setMainServerUrl(url: String)

	fun getMainServerUrl(): String?
}
