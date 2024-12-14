package com.seed.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.seed.main.di.mainModule
import com.seed.mobile.di.appModule
import com.seed.persistence.di.persistenceModule
import com.seed.settings.di.settingsModule
import com.seed.uikit.ui.theme.MobileTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (GlobalContext.getKoinApplicationOrNull() == null) {
			startKoin {
				androidContext(this@MainActivity.applicationContext)

				modules(mainModule, settingsModule, appModule, persistenceModule)
			}
		}

		enableEdgeToEdge()

		setContent {
			MobileTheme {
				App(
					modifier = Modifier
						.fillMaxSize()
				)
			}
		}
	}
}