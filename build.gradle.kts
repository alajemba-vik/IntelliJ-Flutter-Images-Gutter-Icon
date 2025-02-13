import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.alaje.intellijplugins"
version = "1.3"

repositories {
    mavenCentral()
    intellijPlatform{
        defaultRepositories()
    }
}

dependencies {
    implementation(files("libs/svgSalamander-1.1.4.jar"))
    intellijPlatform {
        intellijIdeaCommunity("2024.3.2.2")
        // Must be compatible with IntelliJ version
        plugins("Dart:243.23654.44")
        bundledPlugins("com.intellij.java", "org.jetbrains.kotlin")
        pluginVerifier()
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
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
    // Java 21 is required since 2024.2
    val jvmVersion = "21"
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = jvmVersion
    }
}

tasks.named<RunIdeTask>("runIde") {
    jvmArgumentProviders += CommandLineArgumentProvider {
        listOf("-Didea.kotlin.plugin.use.k2=true")
    }
}