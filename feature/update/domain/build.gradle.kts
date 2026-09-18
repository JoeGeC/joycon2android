plugins {
    id("joycon.kotlin.jvm")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
