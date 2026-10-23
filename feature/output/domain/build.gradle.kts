plugins {
    id("joycon.kotlin.jvm")
}

dependencies {
    api(project(":core:model"))
    api(project(":core:buttonmapping:domain"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
