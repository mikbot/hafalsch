plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.apollographql.apollo3").version("3.7.0")
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
                implementation("com.apollographql.apollo3:apollo-runtime:3.7.0")
            }
        }
    }
}


tasks {
    downloadApolloSchema {
        schema.set("rainbow_ice")
        endpoint.set("https://regenbogen-ice.de/graphql")
    }
}
apollo {
    packageName.set("dev.schlaubi.hafalsch.rainbow_ice")
}
