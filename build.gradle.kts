plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.alaje.learn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.1")
    type.set("IC") // Target IDE Platform

    plugins.set(
        listOf(
            "com.intellij.java",
            "org.jetbrains.kotlin",
            "Dart:241.15989.9",
            /*"io.flutter:80.0.2",*/
            "org.jetbrains.android:241.15989.150"
        )
    )
}

tasks {
    runIde {
        // IDE Development Instance (the "Contents" directory is macOS specific):
        ideDir.set(file("/Applications/Android Studio.app/Contents"))
    }

    buildSearchableOptions {
        enabled = false
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("241.*")
    }


    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
