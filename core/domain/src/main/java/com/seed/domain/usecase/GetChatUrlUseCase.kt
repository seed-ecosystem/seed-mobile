package com.seed.domain.usecase

import com.seed.domain.KeyManager
import com.seed.domain.data.ChatRepository
import com.seed.domain.data.ChatsRepository
import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce
import com.seed.domain.values.ServerUrl
import java.net.URLEncoder

class GetChatUrlUseCase(
	private val chatsRepository: ChatsRepository,
	private val chatRepository: ChatRepository,
	private val keyManager: KeyManager,
) {
	suspend operator fun invoke(chatId: ChatId): String? = getChatShareUrl(chatId)

	private suspend fun getChatShareUrl(chatId: ChatId): String? {
		val chat = chatsRepository.getChat(
			chatId = chatId
		)

		if (chat == null) return null

		val serverUrl = chat.serverUrl
		val chatName = chat.name

		val shareNonce = chatRepository.getMessages(chat.chatId)
			.maxByOrNull { it.nonce.value }
			?.nonce
			?.plus(1)

		if (shareNonce == null) {
			val firstChatKeyNonce = chat.firstChatKeyNonce
			val firstChatKey = keyManager.getKey(chatId, firstChatKeyNonce)

			return buildFinalUrl(
				serverUrl = serverUrl,
				chatName = chatName,
				chatId = chatId.value,
				nonce = firstChatKeyNonce,
				key = firstChatKey ?: return null
			)
		}

		val lastKey = keyManager.getKey(
			chatId = chatId,
			nonce = shareNonce,
		)

		if (lastKey == null) return null

		val finalUrl = buildFinalUrl(serverUrl, chatName, chatId.value, shareNonce, lastKey)


		return finalUrl
	}

	private fun buildFinalUrl(
		serverUrl: ServerUrl,
		chatName: String,
		chatId: String,
		nonce: ServerNonce,
		key: ChatKey
	): String {
		val encodedServerUrl = URLEncoder.encode(serverUrl.value, "UTF-8")
		val encodedChatName = URLEncoder.encode(chatName, "UTF-8")
		val encodedChatId = URLEncoder.encode(chatId, "UTF-8")
		val encodedNonce = URLEncoder.encode(nonce.value.toString(), "UTF-8")
		val encodedKey = URLEncoder.encode(key.value, "UTF-8")

		val finalUrl =
			"https://seed-ecosystem.github.io/seed-web/#/import/$encodedChatName/$encodedChatId/$encodedKey/$encodedNonce/$encodedServerUrl"
		return finalUrl
	}
}
