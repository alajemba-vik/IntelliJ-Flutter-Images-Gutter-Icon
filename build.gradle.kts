plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.alaje.intellijplugins"
version = "1.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/svgSalamander-1.1.4.jar"))
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
            "Dart:241.15989.9"
        )
    )
}

tasks {
    // Optional setup to run on AS
    runIde {
        // IDE Development Instance (the "Contents" directory is macOS specific, update the path to your IDE's directory):
        ideDir.set(file(System.getenv("ANDROID_STUDIO_DIR")))
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
