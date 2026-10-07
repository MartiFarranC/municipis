pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Tesseract per a Android (llegir el nom dels cartells sense connexió): només es publica a JitPack.
        maven("https://jitpack.io") {
            content { includeGroup("cz.adaptech.tesseract4android") }
        }
    }
}

rootProject.name = "DescobreixCatalunya"

include(":app")

// Regles del joc en Kotlin pur. Es pot testejar sense l'SDK d'Android: ./gradlew -p joc test
includeBuild("joc")
