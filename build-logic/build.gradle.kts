plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	google()
	gradlePluginPortal()
}

dependencies {
	api(libs.kotlin.plugin)
	api(libs.android.plugin)
}