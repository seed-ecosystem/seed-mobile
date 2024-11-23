package com.seed.main.presentation.chat.logic

import java.time.LocalDateTime

enum class AuthorType {
	Self,
	Others
}

data class Message(
	val id: String,
	val authorType: AuthorType,
	val authorName: String,
	val messageText: String,
	val dateTime: LocalDateTime,
)

interface ChatScreenUiState {
	val inputFieldValue: String
	val chatName: String

	data class HasData(
		val messages: List<Message>,
		override val inputFieldValue: String,
		override val chatName: String,
	) : ChatScreenUiState

	data class NoMessages(
		override val inputFieldValue: String,
		override val chatName: String
	) : ChatScreenUiState

	data class Loading(
		override val inputFieldValue: String,
		override val chatName: String
	) : ChatScreenUiState

	data class Error(
		override val inputFieldValue: String,
		override val chatName: String
	) : ChatScreenUiState
}