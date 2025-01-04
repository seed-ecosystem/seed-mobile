package com.seed.domain.api

sealed interface ApiResponse<T> {
	data class Success<T>(
		val data: T
	) : ApiResponse<T>

	data class Failure<T>(
		val message: String? = null
	) : ApiResponse<T>
}