plugins {
    id("joycon.android.library.compose")
}

android {
    namespace = "com.joegec.joycon2android.feature.dsu.presentation"
}

dependencies {
    implementation(project(":feature:dsu:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
}
