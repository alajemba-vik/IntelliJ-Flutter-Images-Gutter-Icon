import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.alaje.intellijplugins"
version = "1.2.4"

repositories {
    mavenCentral()
    intellijPlatform{
        defaultRepositories()
    }
}

dependencies {
    implementation(files("libs/svgSalamander-1.1.4.jar"))
    intellijPlatform {
        create("IC", "2024.1.1")
        plugins("Dart:241.15989.9")
        bundledPlugins("com.intellij.java", "org.jetbrains.kotlin")
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }

        productDescriptor {
            releaseVersion = "2024.1.1"
        }
    }
    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}