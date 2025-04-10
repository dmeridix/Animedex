// build.gradle.kts (de nivel de proyecto)
buildscript {
    repositories {
        google() // Repositorio de Google para plugins como Google Services
        mavenCentral() // Repositorio de Maven Central
    }
    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // Asegúrate de usar la última versión
        // Otros classpaths que necesites, por ejemplo:
        // classpath("com.android.tools.build:gradle:X.X.X")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
