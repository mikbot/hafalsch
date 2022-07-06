plugins {
    val kotlin = "1.7.0"
    kotlin("plugin.serialization") version kotlin apply false
}

subprojects {
    repositories {
        mavenCentral()
    }

    group = "dev.schlaubi"
    version = "1.0-SNAPSHOT"
}
