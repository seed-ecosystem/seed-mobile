package com.seed.domain.usecase

import com.seed.domain.crypto.SeedCoder
import com.seed.domain.data.ChatRepository

class GetMessageKeyUseCase(
	private val coder: SeedCoder,
	private val chatRepository: ChatRepository
) {
	suspend operator fun invoke(
		chatId: String,
		nonce: Int,
	): String? {
//		var key: String = chatRepository.getChatKey(chatId) ?: return null
		// todo
		var key = "/uwFt2yxHi59l26H9V8VTN3Kq+FtRewuWNfz1TNVcnM="

		var keyNonce = 0 // TODO

		while (keyNonce != nonce) {
			key = coder.deriveNextKey(key)
			keyNonce++
		}

		return key
	}
}