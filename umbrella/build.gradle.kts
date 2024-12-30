import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

plugins {
    id("module-convention")
}

val deps = listOf<Any>()

dependencies {
    deps.forEach(::api)
}

kotlin {
    @OptIn(ExperimentalSwiftExportDsl::class)
    swiftExport {
        moduleName = "Umbrella"
        flattenPackage = "com.seed.multiplatform.umbrella"
    }
}

android {
    namespace = "com.seed.shared"
}

