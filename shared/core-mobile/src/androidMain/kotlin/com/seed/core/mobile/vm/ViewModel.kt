package com.seed.core.mobile.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

public actual open class ViewModel private constructor(
    public actual val viewModelScope: CoroutineScope
) : ViewModel(viewModelScope) {
    public actual constructor() : this(createViewModelScope())

    public actual override fun onCleared() : Unit = super.onCleared().also {
        viewModelScope.cancel()
    }
}
