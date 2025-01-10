package com.seed.core.mobile.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

public actual open class ViewModel public actual constructor() {
    public actual val viewModelScope: CoroutineScope = createViewModelScope()

    public actual open fun onCleared(): Unit = viewModelScope.cancel()
}
