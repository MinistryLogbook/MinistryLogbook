@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed

import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.aboutlicenses)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "app.ministrylogbook"
        minSdk = 28
        targetSdk = 33
        versionCode = getVersionCode()
        versionName = getTagName()
        resourceConfigurations += listOf("en", "de")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
        }
    }

    signingConfigs {
        create("release") {
            storeFile =
                project.rootDir.resolve("keystore/MinistryLogbook.jks")
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
            applicationIdSuffix = ".debug"
            signingConfig =
                if (hasSigningConfig()) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName(
                        "debug"
                    )
                }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasSigningConfig()) signingConfigs.getByName("release") else null
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
        kotlinCompilerExtensionVersion = "1.4.7"
    }

    packaging {
        resources.excludes += "META-INF/*"
    }

    namespace = "app.ministrylogbook"
}

tasks.withType<KotlinCompile>().all {
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

    implementation(libs.ktoml)
    implementation(libs.aboutlicenses.core)
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.ui.tooling)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
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
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = it
        }
    } catch (e: Exception) {
        return@use ""
    }
    it.toString().trim()
}

fun getVersionCode() = ByteArrayOutputStream().use {
    project.exec {
        commandLine("git", "tag")
        standardOutput = it
    }
    it.toString().trim().split("\n").size
}

fun hasSigningConfig() = System.getenv("SIGNING_STORE_PASSWORD") != null
