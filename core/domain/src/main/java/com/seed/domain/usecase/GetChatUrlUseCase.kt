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
	suspend operator fun invoke(chatId: ChatId): String? = getChatShareUrl(chatId.value)

	private suspend fun getChatShareUrl(chatId: String): String? {
		val chat = chatsRepository.getChat(
			chatId = ChatId(chatId)
		)

		if (chat == null) return null

		val serverUrl = chat.serverUrl
		val chatName = chat.name

		val maxMessageNonce = chatRepository.getMessages(chat.chatId)
			.maxByOrNull { it.nonce.value }
			?.nonce

		if (maxMessageNonce == null) {
			val firstChatKeyNonce = chat.firstChatKeyNonce
			val firstChatKey = keyManager.getKey(ChatId(chatId), firstChatKeyNonce)

			return buildFinalUrl(
				serverUrl = serverUrl,
				chatName = chatName,
				chatId = chatId,
				nonce = firstChatKeyNonce,
				key = firstChatKey ?: return null
			)
		}

		val lastKey = keyManager.getKey(
			chatId = ChatId(chatId),
			nonce = maxMessageNonce,
		)

		if (lastKey == null) return null

		val finalUrl = buildFinalUrl(serverUrl, chatName, chatId, maxMessageNonce, lastKey)


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
