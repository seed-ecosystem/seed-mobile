package com.seed.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.seed.api.util.SeedSocket
import com.seed.domain.GetApplicationCoroutineScope
import com.seed.domain.SeedWorker
import com.seed.domain.SeedWorkerStateHandle
import com.seed.domain.api.SeedApi
import com.seed.domain.data.ChatRepository
import com.seed.domain.saveNewMessages
import com.seed.main.di.mainModule
import com.seed.mobile.di.appModule
import com.seed.persistence.di.persistenceModule
import com.seed.settings.di.settingsModule
import com.seed.uikit.ui.theme.MobileTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val coroutineModule = module {
			single<GetApplicationCoroutineScope> {
				getApplicationCoroutineScope()
			}
		}

		if (GlobalContext.getKoinApplicationOrNull() == null) {

			startKoin {
				androidContext(this@MainActivity.applicationContext)

				modules(mainModule, settingsModule, appModule, persistenceModule, coroutineModule)
			}
		}

		val koin = GlobalContext.getKoinApplicationOrNull()?.koin

		koin?.let { initializeMessaging(it) }

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

	private fun getApplicationCoroutineScope() = object : GetApplicationCoroutineScope {
		override fun invoke(): CoroutineScope = lifecycleScope
	}

	private fun initializeMessaging(koin: Koin) {
		val seedSocket = koin.get<SeedSocket>()
		val seedApi = koin.get<SeedApi>()
		val chatRepository = koin.get<ChatRepository>()
		val worker = koin.get<SeedWorker>()
		val workerStateHandle = koin.get<SeedWorkerStateHandle>()

		seedSocket.initializeSocketConnection(lifecycleScope)
		seedApi.launchConnection(lifecycleScope)
		worker.initializeWorker()
		workerStateHandle.initializeWorkerStateHandle()

		lifecycleScope.launch {
			workerStateHandle.subscribe(
				chatId = "bHKhl2cuQ01pDXSRaqq/OMJeDFJVNIY5YuQB2w7ve+c=",
				nonce = 2370
			)
		}

		lifecycleScope.launch {
			saveNewMessages(
				chatRepository = chatRepository,
				worker = worker,
			)
		}
	}
}
