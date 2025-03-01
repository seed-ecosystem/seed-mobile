package com.seed.domain.data

import com.seed.domain.values.ChatId
import com.seed.domain.values.ChatKey
import com.seed.domain.values.ServerNonce

interface ChatKeyRepository {
	suspend fun insertKeys(chatId: ChatId, keys: List<Pair<ChatKey, ServerNonce>>)

	suspend fun insertChatKey(chatId: ChatId, nonce: ServerNonce, key: ChatKey)

	suspend fun getChatKey(chatId: ChatId, nonce: ServerNonce): ChatKey?

	suspend fun getLastChatKey(chatId: ChatId): GetLastChatKeyResult?

	suspend fun getOldestChatKey(chatId: ChatId): GetOldestChatKeyResult?
}
