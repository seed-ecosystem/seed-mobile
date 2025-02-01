package com.seed.persistence.pref

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class MainServerSharedPreferences(
	private val applicationContext: Context
) {
	companion object {
		private const val SharedPreferencesName = "MainServerSharedPreferences"
		private const val MainServerKey = "MainServerUrl"
	}

	private val preferences = applicationContext.getSharedPreferences(
		SharedPreferencesName,
		MODE_PRIVATE
	)

	fun getMainServerUrl(): String? {
		return preferences.getString(MainServerKey, null)
	}

	fun setMainServerUrl(nickname: String) {
		preferences.edit {
			putString(MainServerKey, nickname)
			commit()
		}
	}
}
