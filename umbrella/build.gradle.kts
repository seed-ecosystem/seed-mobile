plugins {
    id("mobile-module-convention")
}

val deps = listOf(
    libs.kotlinx.coroutines,
    libs.kotlinx.datetime,
    projects.shared.api,
    projects.shared.core,
    projects.shared.coreMobile,
    projects.shared.crypto,
    projects.shared.domain,
    projects.shared.feature.main,
    // projects.shared.domain.settings,
)

dependencies {
    deps.forEach(::commonMainApi)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Umbrella"
            deps.forEach(::export)
            isStatic = true
        }
    }
}

android {
    namespace = "com.seed.shared"
}

