plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.m7animedex"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.m7animedex"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Glide
    implementation(libs.glide) // Dependencia principal de Glide
    // Otras dependencias
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.core.splashscreen)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Parcelize
    implementation(libs.kotlin.parcelize.runtime)

    // Coroutines y MockWebServer
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.mpandroidchart)

}