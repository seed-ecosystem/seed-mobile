plugins {
	id("kmp-module-convention")
	alias(libs.plugins.serialization)
}

dependencies {
	commonMainApi(libs.kotlinx.coroutines)
	commonMainApi(libs.kotlinx.serialization.json)
}