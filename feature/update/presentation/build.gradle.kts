plugins {
    id("joycon.android.library.compose")
}

android {
    namespace = "com.joegec.joycon2android.update.presentation"
}

dependencies {
    implementation(project(":feature:update:domain"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}
