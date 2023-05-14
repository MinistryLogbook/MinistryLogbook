@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.github.danieldaeschle.ministrylogbook"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = getTagName()
        resourceConfigurations += listOf("en", "de")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                )
            }
        }
    }

    signingConfigs {
        create("release") {
            storeFile = project.rootDir.resolve("keystore/MinistryLogbook.jks") // file("../keystore/MinistryLogbook.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildTypes {
        debug {
            versionNameSuffix = if (getTagName() == "") getGitHash() else ".${getGitHash()}"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }

    packaging {
        resources.excludes += "META-INF/*"
    }

    namespace = "com.github.danieldaeschle.ministrylogbook"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)

    implementation(libs.koin)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.ui.tooling)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.navigation.material)
    implementation(libs.accompanist.navigation.animation)

    debugImplementation(libs.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

fun getGitHash() = ByteArrayOutputStream().use {
    project.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = it
    }
    it.toString().trim()
}

fun getTagName() = ByteArrayOutputStream().use {
    try {
        project.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = it
        }
    } catch (e: Exception) {
        return@use ""
    }
    it.toString().trim()
}
