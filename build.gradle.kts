@file:Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.aboutlicenses) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
