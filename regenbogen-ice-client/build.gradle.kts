plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `hafalsch-publishing`
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
                api(projects.clientCommon)
            }
        }
    }
}
