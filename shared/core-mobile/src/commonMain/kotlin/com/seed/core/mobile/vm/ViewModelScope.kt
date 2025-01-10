package com.seed.core.mobile.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.ThreadLocal

public fun createCoroutineContext(): CoroutineContext =
    (SupervisorJob() + Dispatchers.Main.immediate)

@ThreadLocal
public var createViewModelScope: () -> CoroutineScope = {
    CoroutineScope(createCoroutineContext())
}
