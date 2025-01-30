package com.seed.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T> Flow<T>.collectIn(scope: CoroutineScope, block: suspend (T) -> Unit) {
	onEach(block).launchIn(scope)
}
