plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.update.data"
}

dependencies {
    implementation(project(":feature:update:domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    testImplementation(libs.json)
}
