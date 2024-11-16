plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "com.seed.uikit"
	compileSdk = 35

	defaultConfig {
		minSdk = 29

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {
	api(platform(libs.androidx.compose.bom))

	api(libs.androidx.core.ktx)
	api(libs.androidx.lifecycle.runtime.ktx)
	api(libs.androidx.activity.compose)
	api(libs.androidx.ui)
	api(libs.androidx.ui.graphics)
	api(libs.androidx.ui.tooling.preview)
	api(libs.androidx.material3)
	api(libs.androidx.appcompat)

	api(libs.coil.kt)

	debugApi(libs.androidx.ui.tooling)

	testImplementation(libs.junit)

	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}