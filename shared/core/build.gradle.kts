plugins {
	id("kmp-module-convention")
	alias(libs.plugins.serialization)
}

kotlin {
	explicitApi()
}

dependencies {
	commonMainApi(libs.kotlinx.datetime)
	commonMainApi(libs.kotlinx.coroutines)
	commonMainApi(libs.kotlinx.serialization.json)
}