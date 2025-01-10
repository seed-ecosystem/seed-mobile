plugins {
	id("mobile-module-convention")
	alias(libs.plugins.serialization)
}

android {
	namespace = "com.seed.core.mobile"
}

kotlin {
	explicitApi()
}

dependencies {
	androidMainApi(libs.androidx.lifecycle.viewmodel)
	androidMainApi(libs.androidx.core.ktx)

	commonMainApi(projects.shared.core)
}