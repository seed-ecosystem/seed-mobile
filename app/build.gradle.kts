plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.serialization)
}

android {
	namespace = "com.seed.mobile"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.seed.mobile"
		minSdk = 29
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName("debug")
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
	implementation(project(":core:uikit"))
	implementation(project(":core:domain"))
	implementation(project(":core:data"))
	implementation(project(":core:crypto"))
	implementation(project(":core:persistence"))

	implementation(project(":feature:main"))

	implementation(platform(libs.androidx.compose.bom))
	implementation(platform(libs.koin.bom))

	implementation(libs.androidx.navigation)

	implementation(libs.koin.core)
	implementation(libs.koin.android)
	implementation(libs.koin.androidx.compose)
	implementation(libs.koin.compose)
	implementation(libs.koin.compose.viewmodel)

	implementation(libs.kotlinx.serialization.json)

	testImplementation(libs.junit)

	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)

	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)
}