package com.seed.domain.data

interface NicknameRepository {
	fun setNickname(nickname: String)

	fun getNickname(): String?
}