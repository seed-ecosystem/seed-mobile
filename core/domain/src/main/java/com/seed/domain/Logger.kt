package com.seed.domain

interface Logger {
	fun d(tag: String, message: String)

	fun e(tag: String, message: String)
}