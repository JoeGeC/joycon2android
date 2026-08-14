plugins {
    id("joycon.android.library.compose")
}

android {
    namespace = "com.joegec.joycon2android.core.buttonmapping.presentation"
}

dependencies {
    implementation(project(":core:buttonmapping:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
}
