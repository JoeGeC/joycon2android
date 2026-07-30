pluginManagement {
    includeBuild("build-logic")
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Joycon2Android"
include(":app")
include(":core:model")
include(":core:designsystem")
include(":core:session")
include(":core:emulatorconfig")
include(":feature:dsu:domain")
include(":feature:dsu:data")
include(":feature:dsu:presentation")
include(":feature:assignment:domain")
include(":feature:assignment:data")
include(":feature:assignment:presentation")
include(":feature:gamepad:domain")
include(":feature:gamepad:data")
include(":feature:gamepad:presentation")
include(":feature:connection:domain")
include(":feature:connection:data")
include(":feature:connection:presentation")
include(":konsist")
