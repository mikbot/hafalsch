plugins {
    val kotlin = "1.7.0"
    kotlin("multiplatform") version kotlin apply false
    kotlin("plugin.serialization") version kotlin apply false
}

group = "dev.schlaubi"
version = "1.0-SNAPSHOT"

subprojects {
    repositories {
        mavenCentral()
    }
}
