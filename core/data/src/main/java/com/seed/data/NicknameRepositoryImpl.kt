package com.seed.data

import com.seed.domain.data.NicknameRepository
import com.seed.persistence.pref.NicknameSharedPreferences

class NicknameRepositoryImpl(
	private val nicknameSharedPreferences: NicknameSharedPreferences,
) : NicknameRepository {
	override fun setNickname(nickname: String) {
		nicknameSharedPreferences.setNickname(nickname)
	}

	override fun getNickname(): String? {
		return nicknameSharedPreferences.getNickname()
	}
}