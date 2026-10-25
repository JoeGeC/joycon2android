plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.output.data"
}

dependencies {
    implementation(project(":feature:output:domain"))
    implementation(project(":core:model"))
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
}
