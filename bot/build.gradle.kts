import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

plugins {
    id("com.google.devtools.ksp") version "1.7.20-1.0.8"
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("dev.schlaubi.mikbot.gradle-plugin") version "2.6.3"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/service/local/repositories/snapshots/content/")
}

dependencies {
    @Suppress("DependencyOnStdlib")
    compileOnly(kotlin("stdlib-jdk8"))
    mikbot("dev.schlaubi", "mikbot-api", "3.15.0-SNAPSHOT")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "2.2.0")
    ksp("com.kotlindiscord.kord.extensions", "annotation-processor", "1.5.5-SNAPSHOT")
    plugin("dev.schlaubi", "mikbot-ktor", "2.8.0")

    implementation(projects.marudorClient)
    implementation(projects.traewellingClient)
    implementation(projects.regenbogenIceClient)
    implementation("io.ktor", "ktor-client-logging", "2.0.3")
    implementation("info.debatty", "java-string-similarity", "2.0.0")
    implementation(platform("io.ktor:ktor-bom:2.1.3"))
    implementation("io.ktor", "ktor-server-auth-jvm")
    implementation("io.ktor", "ktor-server-sessions-jvm")
}

configurations {
    runtimeClasspath {
        exclude("io.ktor", "ktor-resources-jvm")
        exclude("io.ktor", "ktor-server-core")
    }
}

mikbotPlugin {
    description.set("Plugin providing Discord Trainzzz")
    pluginId.set("hafalsch")
    bundle.set("hafalsch")
    provider.set("Schlaubi")
    license.set("MIT")
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin/"))
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "18"
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

    val generateDefaultResourceBundle = task<GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
        defaultLocale.set(Locale("en", "GB"))
    }

    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }

    assembleBot {
        bundledPlugins.add("ktor@2.8.0")
    }
}
