import org.jmailen.gradle.kotlinter.tasks.InstallPrePushHookTask

val installKotlinterPrePushHook by tasks.creating(InstallPrePushHookTask::class)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.aboutlicenses) apply false
    alias(libs.plugins.compose.compiler) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("check") {
    dependsOn("installKotlinterPrePushHook")
}
