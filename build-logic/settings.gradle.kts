// build-logic/settings.gradle.kts
// Included build que centraliza convention plugins de PrideQuiz.
// Referencia el mismo Version Catalog del proyecto raiz para evitar duplicar versiones.

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
