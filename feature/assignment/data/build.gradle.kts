plugins {
    id("joycon.kotlin.jvm")
}

dependencies {
    implementation(project(":feature:assignment:domain"))
    testImplementation(libs.junit)
}
