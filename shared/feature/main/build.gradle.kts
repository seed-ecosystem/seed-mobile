plugins {
	id("mobile-module-convention")
	alias(libs.plugins.serialization)
}

android {
	namespace = "com.seed.shared.main"
}

dependencies {
	commonMainApi(projects.shared.coreMobile)
	commonMainImplementation(projects.shared.domain)
}
