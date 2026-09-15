plugins {
    id("joycon.kotlin.jvm")
}

dependencies {
    api(project(":core:buttonmapping:domain"))
    testImplementation(libs.junit)
}
