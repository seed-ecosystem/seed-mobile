package com.seed.domain.usecase

import com.seed.domain.Logger
import com.seed.domain.crypto.SeedCoder
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import java.security.SecureRandom
import java.util.Base64

sealed interface CreateChatResult {
	data object Success : CreateChatResult
	data object Failure : CreateChatResult
}

class CreateChatUseCase(
	private val addChatUseCase: AddChatUseCase,
	private val coder: SeedCoder,
	private val logger: Logger,
) {
	suspend operator fun invoke(
		chatName: String,
		serverUrl: ServerUrl,
	): CreateChatResult {
		val chatId = ChatId(generateRandom256bitBase64String())
		val initialNonce = ServerNonce(0)
		val initialPrivateKey = ChatKey(generateRandom256bitBase64String())

		addChatUseCase(
			key = initialPrivateKey,
			keyNonce = initialNonce,
			name = chatName,
			chatId = chatId,
			serverUrl = serverUrl
		)

		return CreateChatResult.Success
	}
}

private fun generateRandom256bitBase64String(): String {
	val bitLength: Int = 256
	val byteLength = bitLength / 8
	val randomBytes = ByteArray(byteLength)
	SecureRandom().nextBytes(randomBytes)
	return Base64.getEncoder().encodeToString(randomBytes)
}
