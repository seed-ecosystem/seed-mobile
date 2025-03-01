package com.seed.domain.data

import com.seed.domain.values.ServerUrl

interface SettingsRepository {
	fun setNickname(nickname: String)

	fun getNickname(): String?

	fun setMainServerUrl(url: ServerUrl)

	fun getMainServerUrl(): ServerUrl?
}
