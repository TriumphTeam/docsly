import java.net.URI

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

val dokkaOutputDir = "$buildDir/dokka"

tasks {
    dokkaHtml {
        outputDirectory.set(file(dokkaOutputDir))
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

publishing {
    publications {
        val publication by creating(MavenPublication::class) {
            artifactId = project.name
            from(components["java"])
            artifact(javadocJar.get())

            pom {
                name.set("Doclopedia Dokka plugin")
                description.set("This the Doclopedia plugin for Dokka")
                url.set("https://github.com/TriumphTeam/doclopedia")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
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
                    connection.set("scm:git:git://github.com/TriumphTeam/doclopedia.git")
                    url.set("https://github.com/TriumphTeam/doclopedia/tree/master")
                }
            }
        }

        signing {
            sign(publication)
        }
    }

    repositories {
        maven {
            url = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USER")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}
