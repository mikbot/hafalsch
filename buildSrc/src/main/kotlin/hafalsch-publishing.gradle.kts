import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.util.Base64

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

val dokkaJar by tasks.registering(Jar::class) {
    dependsOn("dokkaHtml")
    archiveClassifier.set("javadoc")
    from(tasks.getByName("dokkaHtml"))
}

publishing {
    repositories {
        maven {
            setUrl("https://schlaubi.jfrog.io/artifactory/mikbot/")
            credentials {
                username = System.getenv("JFROG_USER")
                password = System.getenv("JFROG_KEY")
            }
        }
    }

    publications {
        withType<MavenPublication> {
            artifact(dokkaJar)
            pom {
                name.set(project.name)
                description.set("Hafas stuff")
                url.set("https://github.com/mikbot/hafalsch")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/mikbot/hafalsch/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        name.set("Michael Rittmeister")
                        email.set("mail@schlaubi.me")
                        organizationUrl.set("https://schlau.bi")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/mikbot/hafalsch.git")
                    developerConnection.set("scm:git:https://github.com/mikbot/hafalsch.git")
                    url.set("https://github.com/DRSchlaubi/stdx.kt")
                }
            }
        }
    }
}

signing {
    val signingKey = findProperty("signingKey")?.toString()
    val signingPassword = findProperty("signingPassword")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(
            String(Base64.getDecoder().decode(signingKey.toByteArray())),
            signingPassword
        )

        publishing.publications.withType<MavenPublication> {
            sign(this)
        }
    }

}

fun KotlinTarget.publicationName() = targetName.take(1).toUpperCase() + targetName.drop(1)
