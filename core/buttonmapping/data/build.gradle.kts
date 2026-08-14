plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.core.buttonmapping.data"
}

dependencies {
    implementation(project(":core:buttonmapping:domain"))
    implementation(project(":core:model"))
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
}
