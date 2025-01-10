plugins {
	id("kmp-module-convention")
	alias(libs.plugins.serialization)
}

dependencies {
	commonMainApi(projects.shared.domain)
	commonMainImplementation(libs.kotlinx.serialization.json)

	commonTestImplementation(kotlin("test"))
}
