plugins {
	id("kmp-module-convention")
	alias(libs.plugins.serialization)
}

dependencies {
	commonMainApi(projects.core.domain)
	commonMainImplementation(libs.kotlinx.serialization.json)
}
