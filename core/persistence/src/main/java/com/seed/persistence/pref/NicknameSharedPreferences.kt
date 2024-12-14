package com.seed.persistence.pref

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class NicknameSharedPreferences(
	private val applicationContext: Context
) {
	companion object {
		private const val SharedPreferencesName = "NicknameSharedPreferences"
		private const val NicknameKey = "Nickname"
	}

	private val preferences = applicationContext.getSharedPreferences(
		SharedPreferencesName,
		MODE_PRIVATE
	)

	fun getNickname(): String? {
		return preferences.getString(NicknameKey, null)
	}

	fun setNickname(nickname: String) {
		preferences.edit {
			putString(NicknameKey, nickname)
			commit()
		}
	}
}