import utilities.VersionType
import utilities.isSnapshot
import utilities.writeVersion

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("dev.architectury.loom")
    id("net.nemerosa.versioning")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven("https://maven.impactdev.net/repository/development/") {
            name = "ImpactDev-Public"
            credentials {
                username = System.getenv("COBBLEMON_MAVEN_USER")
                password = System.getenv("COBBLEMON_MAVEN_PASSWORD")
            }
        }

        maven {
            val snapshot = project.isSnapshot()

            val releases = uri("https://artefacts.cobblemon.com/releases")
            val snapshots = uri("https://artefacts.cobblemon.com/snapshots")

            url = if (snapshot) snapshots else releases
            name = "Reposilite.${if (snapshot) "Snapshots" else "Releases"}"
            credentials {
                username = System.getenv("COBBLEMON_REPOSILITE_USERNAME")
                password = System.getenv("COBBLEMON_REPOSILITE_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>(project.name) {
            artifact(tasks.remapJar)
            artifact(tasks.remapSourcesJar)

            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)

            groupId = "com.cobblemon"
            artifactId = project.findProperty("maven.artifactId")?.toString() ?: project.name
            version = project.writeVersion(VersionType.PUBLISHING)
            pom {
                properties = mapOf(
                    "gitCommit" to versioning.info.commit
                )
            }
        }
    }
}