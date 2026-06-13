import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Android library module targeting minSdk 24 / Java 11, matching the app. AGP 9 provides
 * Kotlin built-in, so the Kotlin plugin is not applied separately (doing so collides on
 * the `kotlin` extension).
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        extensions.configure<LibraryExtension> {
            compileSdk = 36
            defaultConfig {
                minSdk = 24
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            testOptions {
                // android.util.Log no-ops on the JVM instead of throwing in unit tests
                unitTests.isReturnDefaultValues = true
            }
        }
    }
}
