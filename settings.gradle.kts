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

include(":umbrella")

include(":android:app")
include(":android:uikit")
include(":android:persistence")
include(":android:data")
include(":android:feature:main")
include(":android:feature:settings")

include(":shared:domain")
include(":shared:crypto")
include(":shared:api")
include(":shared:core")
include(":shared:core-mobile")

include(":shared:feature:main")

