import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

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
    deps.forEach(::api)
}

kotlin {
    @OptIn(ExperimentalSwiftExportDsl::class)
    swiftExport {
        moduleName = "Umbrella"
        flattenPackage = "com.seed.multiplatform.umbrella"
        deps.forEach(::export)
    }
}

android {
    namespace = "com.seed.shared"
}

