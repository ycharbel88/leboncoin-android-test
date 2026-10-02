plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "fr.leboncoin.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependencies {
        implementation(project(":domain"))

        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)

        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        ksp(libs.androidx.room.compiler)
        implementation(libs.paging.runtime)
        implementation(libs.room.paging)
        testImplementation(libs.paging.testing)
        testImplementation(libs.androidx.test.core)

        implementation(libs.retrofit.core)
        implementation(libs.retrofit.kotlin.serialization)
        implementation(libs.okhttp.logging)

        implementation(libs.kotlin.serialization.json)
        testImplementation(libs.cash.turbine)
        testImplementation(libs.org.robolectric)
        testImplementation(libs.mockito.core)
        testImplementation(libs.mockito.kotlin)
        testImplementation(libs.junit)
        testImplementation(libs.kotlinx.coroutines.test)
       }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
