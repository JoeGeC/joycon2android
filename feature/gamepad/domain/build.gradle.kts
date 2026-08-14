plugins {
    id("joycon.kotlin.jvm")
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:emulatorconfig"))
    implementation(project(":core:buttonmapping:domain"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
