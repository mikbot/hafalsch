import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.8.10"))
    implementation(kotlin("serialization", "1.8.10"))
    implementation("org.jetbrains.dokka", "dokka-gradle-plugin", "1.7.0")
    implementation(gradleApi())
    implementation(localGroovy())
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            languageVersion = "1.5"
        }
    }
}
