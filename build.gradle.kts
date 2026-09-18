import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        if (System.getenv("CI") == "true") {
            // GitHub Actions has no local Android Studio install. Quail 4
            // (2026.1.4) isn't indexed in JetBrains' Android Studio release
            // list yet, so we pin CI to 2026.1.2 instead — an earlier release
            // on the same IntelliJ Platform branch (261), which is what
            // actually matters for binary/API compatibility here.
            androidStudio("2026.1.2")
        } else {
            // Local dev machine: use the already-installed Android Studio.
            local("C:/Users/GODARK/AppData/Local/Programs/Android Studio")
        }
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.jetbrains.android")
        testFramework(TestFrameworkType.Platform)
    }
}