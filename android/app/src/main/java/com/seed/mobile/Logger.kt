package com.seed.mobile

import android.util.Log
import com.seed.domain.Logger

class LoggerImpl : Logger {
	override fun d(tag: String, message: String) {
		Log.d(tag, message)
	}

	override fun e(tag: String, message: String) {
		Log.e(tag, message)
	}
}