plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.feature.dsu.data"
}

dependencies {
    implementation(project(":feature:dsu:domain"))
    implementation(project(":core:model"))
    testImplementation(libs.junit)
}
