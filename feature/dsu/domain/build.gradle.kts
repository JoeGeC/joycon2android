plugins {
    id("joycon.kotlin.jvm")
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:emulatorconfig"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
