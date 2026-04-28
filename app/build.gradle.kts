plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.aboutlicenses)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
    id("org.jetbrains.kotlin.plugin.parcelize")
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

    }

    androidResources {
        @Suppress("UnstableApiUsage")
        localeFilters += listOf("en", "de")
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
        kotlin.directories.add("src/$name/kotlin")
    }

    sourceSets.getByName("androidTest").assets.directories.add("$projectDir/schemas")

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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs.keepDebugSymbols += listOf(
            "**/libandroidx.graphics.path.so",
            "**/libdatastore_shared_counter.so"
        )
    }

    namespace = "app.ministrylogbook"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}

androidComponents {
    onVariants(selector().withBuildType("release")) {
        it.packaging.resources.excludes.add("META-INF/*.version")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    debugImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
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

    debugImplementation(libs.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
}

fun gitOutput(vararg args: String) = providers.exec {
    commandLine("git", *args)
    isIgnoreExitValue = true
}.standardOutput.asText.get().trim()

fun getGitHash() = gitOutput("rev-parse", "--short", "HEAD")

fun getTagName() = gitOutput("describe", "--tags", "--abbrev=0")

fun getVersionCode() = gitOutput("tag")
    .lines()
    .filter { it.isNotBlank() }
    .size

fun hasSigningConfig() = System.getenv("SIGNING_STORE_PASSWORD") != null
