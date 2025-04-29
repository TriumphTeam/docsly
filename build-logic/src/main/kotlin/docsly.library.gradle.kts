import dev.triumphteam.root.repository.Repository

plugins {
    id("docsly.base")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

// TODO fix this
/*
tasks {
    dokkaHtml {
        outputDirectory.set(file(dokkaOutputDir))
    }
}
*/

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
}

root {
    configurePublishing {
        configure {
            artifactId = project.name
            from(components["java"])
            artifact(javadocJar.get())

            pom {
                name.set("Docsly Dokka plugin")
                description.set("This the Docsly plugin for Dokka")
                url.set("https://github.com/TriumphTeam/docsly")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/docsly/MIT")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("TriumphTeam")
                        name.set("TriumphTeam")
                        organization.set("TriumphTeam")
                        organizationUrl.set("https://triumphteam.dev/")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/TriumphTeam/docsly.git")
                    url.set("https://github.com/TriumphTeam/docsly/tree/master")
                }
            }

            sign()

            snapshotsRepo(Repository.TRIUMPH_SNAPSHOTS)
            releasesRepo(Repository.CENTRAL)
        }
    }
}
