import org.jmailen.gradle.kotlinter.tasks.InstallPrePushHookTask

val installKotlinterPrePushHook by tasks.registering(InstallPrePushHookTask::class)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.aboutlicenses) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.room) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
