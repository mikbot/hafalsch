plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "18"
            }
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project.dependencies.platform("io.ktor:ktor-bom:2.0.2"))
                api("io.ktor:ktor-client-core")
                api("io.ktor:ktor-client-resources")
                api("io.ktor:ktor-client-content-negotiation")
                api("io.ktor:ktor-serialization-kotlinx-json")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }

        getByName("jvmMain") {
            dependencies {
                api("io.ktor:ktor-client-okhttp")
            }
        }
        getByName("jsMain") {
            dependencies {
                api("io.ktor:ktor-client-js")
            }
        }
    }
}
