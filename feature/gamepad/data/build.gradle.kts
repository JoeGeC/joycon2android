plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.gamepad.data"
}

dependencies {
    implementation(project(":feature:gamepad:domain"))
    implementation(project(":core:model"))
    api(libs.shizuku.api)
    api(libs.shizuku.provider)
}
