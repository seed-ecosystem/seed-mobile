package com.seed.domain

import kotlinx.coroutines.CoroutineScope

interface GetApplicationCoroutineScope {
	operator fun invoke(): CoroutineScope
}
