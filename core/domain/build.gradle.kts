plugins {
	id("java-library")
	alias(libs.plugins.jetbrains.kotlin.jvm)
	alias(libs.plugins.serialization)
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
	compilerOptions {
		jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
	}
}

dependencies {
	api(libs.kotlinx.coroutines)
	api(libs.kotlinx.serialization.json)
}