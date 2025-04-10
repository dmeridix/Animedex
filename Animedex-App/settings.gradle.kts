pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")   // Para Android plugins
                includeGroupByRegex("com\\.google.*")    // Para Google plugins
                includeGroupByRegex("androidx.*")        // Para AndroidX plugins
            }
        }
        mavenCentral() // Repositorio central de Maven
        gradlePluginPortal() // Repositorio de Gradle Plugins
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // Asegura que no se use repositorios locales del proyecto
    repositories {
        google() // Repositorio de Google para dependencias
        mavenCentral() // Repositorio central de Maven
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "M7 Animedex"
include(":app") // Incluir el módulo de la aplicación

// Aquí se añade el bloque `buildscript`
buildscript {
    repositories {
        google() // Repositorio de Google para los plugins necesarios
        mavenCentral() // Repositorio central de Maven
        maven { url = uri("https://jitpack.io") }

    }
    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // Asegúrate de usar la última versión de google-services
        // Otros classpaths si los necesitas, como el plugin de Android o Kotlin
    }
}
