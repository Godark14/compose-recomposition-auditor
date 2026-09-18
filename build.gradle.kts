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
            // GitHub Actions has no local Android Studio install — download instead.
            androidStudio(providers.gradleProperty("platformVersion"))
        } else {
            // Local dev machine: use the already-installed Android Studio,
            // since androidStudio(version) couldn't resolve Quail 4 at the
            // time this was set up (see gradle.properties comment).
            local("C:/Users/GODARK/AppData/Local/Programs/Android Studio")
        }
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.jetbrains.android")
        testFramework(TestFrameworkType.Platform)
    }
}