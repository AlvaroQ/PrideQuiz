// build-logic/build.gradle.kts
// Configura el included build que contiene los convention plugins.
// Los plugins de AGP y Kotlin se declaran como compileOnly para acceder a sus APIs
// sin agregarlos al classpath de runtime (el proyecto principal ya los trae).

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

// Registro de los convention plugins disponibles para el proyecto.
// El id es el que se usa con apply plugin: o id() en los modulos.
gradlePlugin {
    plugins {
        register("kotlinLibrary") {
            id = "pridequiz.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "pridequiz.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
