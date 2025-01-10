plugins {
	id("kmp-module-convention")
	alias(libs.plugins.serialization)

}

dependencies {
	commonMainApi(projects.shared.domain)

	commonMainImplementation(libs.ktor.client.websockets)
	commonMainImplementation(libs.ktor.client.core)
	commonMainImplementation(libs.ktor.client.cio)
	commonMainImplementation(libs.ktor.client.logging)

	commonMainImplementation(libs.kotlinx.serialization.json)
}