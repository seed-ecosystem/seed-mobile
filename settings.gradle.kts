enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

rootProject.name = "mobile"

includeBuild("build-logic")
include(":app")
include(":core:uikit")
include(":core:persistence")

include(":core:domain")
include(":umbrella")

include(":feature:main")
include(":core:data")
include(":core:crypto")
include(":core:api")
include(":feature:settings")
