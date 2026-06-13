plugins {
    id("joycon.kotlin.jvm")
}

// Sits above the feature domains: depends on their interfaces/logic, never the reverse.
dependencies {
    api(project(":core:model"))
    api(project(":feature:connection:domain"))
    api(project(":feature:assignment:domain"))
    api(libs.kotlinx.coroutines.core)
}
