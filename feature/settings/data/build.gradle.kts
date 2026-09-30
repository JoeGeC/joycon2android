plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.settings.data"
}

dependencies {
    implementation(project(":feature:settings:domain"))
    implementation(project(":core:model"))
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
}
