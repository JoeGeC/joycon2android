plugins {
    id("joycon.kotlin.jvm")
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
