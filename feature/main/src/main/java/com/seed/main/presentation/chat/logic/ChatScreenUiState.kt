package com.seed.main.presentation.chat.logic

import com.seed.domain.api.SocketConnectionState
import java.time.LocalDateTime

sealed interface Message {
	val nonce: Int
	val authorName: String
	val messageText: String
	val dateTime: LocalDateTime

	data class SelfMessage(
		override val nonce: Int,
		override val authorName: String,
		override val messageText: String,
		override val dateTime: LocalDateTime,
		val isSending: Boolean = false,
		val isSendFailed: Boolean = false,
	) : Message

	data class OthersMessage(
		override val nonce: Int,
		override val authorName: String,
		override val messageText: String,
		override val dateTime: LocalDateTime
	) : Message
}

interface ChatScreenUiState {
	val connectionState: SocketConnectionState
	val inputFieldValue: String
	val chatName: String

	data class HasData(
		val messages: List<Message>,
		override val inputFieldValue: String,
		override val chatName: String,
		override val connectionState: SocketConnectionState,
	) : ChatScreenUiState

	data class NoMessages(
		override val inputFieldValue: String,
		override val chatName: String,
		override val connectionState: SocketConnectionState,
	) : ChatScreenUiState

	data class Loading(
		override val inputFieldValue: String,
		override val chatName: String,
		override val connectionState: SocketConnectionState,
	) : ChatScreenUiState

	data class Error(
		override val inputFieldValue: String,
		override val chatName: String,
		override val connectionState: SocketConnectionState,
	) : ChatScreenUiState
}