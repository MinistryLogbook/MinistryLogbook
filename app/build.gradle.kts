import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.aboutlicenses)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "app.ministrylogbook"
        minSdk = 28
        targetSdk = 36
        versionCode = getVersionCode()
        versionName = getTagName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
        }
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        localeFilters += listOf("en", "de")
    }

    room {
        schemaDirectory("$projectDir/schemas")
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

    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    namespace = "app.ministrylogbook"
}

androidComponents {
    onVariants(selector().withBuildType("release")) {
        it.packaging.resources.excludes.add("META-INF/*.version")
    }
}

tasks.withType<KotlinCompile>().all {
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
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

    implementation(libs.konfetti)
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

    debugImplementation(libs.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

fun getGitHash() = ByteArrayOutputStream().use {
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = it
    }
    it.toString().trim()
}

fun getTagName() = ByteArrayOutputStream().use {
    try {
        exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = it
        }
    } catch (e: Exception) {
        return@use ""
    }
    it.toString().trim()
}

fun getVersionCode() = ByteArrayOutputStream().use {
    exec {
        commandLine("git", "tag")
        standardOutput = it
    }
    it.toString().trim().split("\n").size
}

fun hasSigningConfig() = System.getenv("SIGNING_STORE_PASSWORD") != null
