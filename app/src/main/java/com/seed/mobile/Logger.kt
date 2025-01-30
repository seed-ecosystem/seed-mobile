package com.seed.mobile

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.seed.domain.Logger

class LoggerImpl : Logger {
	override fun d(tag: String, message: String) {
		Log.d(tag, message)
		Firebase.crashlytics.log("D\t$tag\t$message")
	}

	override fun e(tag: String, message: String) {
		Log.e(tag, message)
		Firebase.crashlytics.log("E\t$tag\t$message")
	}
}
