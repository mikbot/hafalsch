import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp") version "1.7.0-1.0.6"
    kotlin("jvm")
    id("dev.schlaubi.mikbot.gradle-plugin") version "2.4.1"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/service/local/repositories/snapshots/content/")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    mikbot("dev.schlaubi", "mikbot-api", "3.3.0-SNAPSHOT")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "2.2.0")
    ksp("com.kotlindiscord.kord.extensions", "annotation-processor", "1.5.5-MIKBOT-SNAPSHOT")

    implementation(projects.marudorClient)
    implementation("info.debatty:java-string-similarity:2.0.0")
}

mikbotPlugin {
    description.set("Plugin providing Discord Trainzzz")
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
        }
    }
}
