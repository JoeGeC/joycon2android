plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.connection.data"
}

dependencies {
    implementation(project(":feature:connection:domain"))
    implementation(project(":core:model"))
    testImplementation(libs.junit)
}
