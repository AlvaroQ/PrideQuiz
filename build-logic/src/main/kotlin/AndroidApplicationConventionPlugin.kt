// AndroidApplicationConventionPlugin.kt
// Convention plugin para el modulo app (com.android.application).
// Centraliza: compileSdk, minSdk, targetSdk, Java compatibility, Kotlin JVM target.
// La configuracion especifica de app (applicationId, signing, buildTypes, AdMob, etc.)
// permanece en app/build.gradle.
//
// Uso en modulo: id("pridequiz.android.application")

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 26
                    targetSdk = 36
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                kotlin {
                    jvmToolchain(17)
                }
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
        }
    }
}
