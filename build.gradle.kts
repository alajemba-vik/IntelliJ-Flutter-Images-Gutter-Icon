import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.alaje.intellijplugins"
version = "1.4.1"

repositories {
    mavenCentral()
    intellijPlatform{
        defaultRepositories()
    }
}

dependencies {
    implementation(files("libs/svgSalamander-1.1.4.jar"))
    intellijPlatform {
        local("/Applications/Android Studio.app")
        //androidStudio("2024.1.1.11")
        //androidStudio("2024.2.2.15")
        //intellijIdeaCommunity("2024.3.2.2")

        // Must be compatible with IntelliJ version
        plugins("Dart:241.15989.9")
        bundledPlugins("com.intellij.java", "org.jetbrains.kotlin")
        pluginVerifier()
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241.14494.240"
            untilBuild = "241.*"
        }
        val changeLogFile = Paths.get("CHANGELOG.md")
        changeNotes = Files.readString(changeLogFile)
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
    val jvmVersion = "17"
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = jvmVersion
    }
}
