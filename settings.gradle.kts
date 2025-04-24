rootProject.name = "tamias"

dependencyResolutionManagement {
    repositories {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        mavenCentral()
        maven("https://reposilite.worldseed.online/public")
        maven {
            name = "OneLiteFeatherRepository"
            url = uri("https://repo.onelitefeather.dev/onelitefeather")
            if (System.getenv("CI") != null) {
                credentials {
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            } else {
                credentials(PasswordCredentials::class)
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
    versionCatalogs {
        create("libs") {
            version("minestom", "1.5.1")
            version("shadow", "8.3.6")
            version("bom", "1.1.2")
            version("projectiles", "2.1.1")
            version("aonyx", "0.1.0")

            library("microtus.bom", "net.onelitefeather.microtus", "bom").versionRef("minestom")
            library("mycelium.bom", "net.theevilreaper.mycelium.bom", "mycelium-bom").versionRef("bom")
            library("game.bom", "net.onelitefeather.aonyx.bom", "aonyx-bom").versionRef("aonyx")


            library("atlas.projectiles", "ca.atlasengine", "atlas-projectiles").versionRef("projectiles")

            library("minestom", "net.onelitefeather.microtus", "Microtus").withoutVersion()
            library("minestom-test", "net.onelitefeather.microtus.testing", "testing").withoutVersion()
            library("junit.api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit.engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("mockito.core", "org.mockito", "mockito-core").withoutVersion()
            library("mockito.junit", "org.mockito", "mockito-junit-jupiter").withoutVersion()

            library("aves", "de.icevizion.lib", "aves").withoutVersion()
            library("xerus", "net.theevilreaper.xerus", "xerus").withoutVersion()

            plugin("shadow", "com.gradleup.shadow").versionRef("shadow")
        }
    }
}
include("common")
include("game")
include("setup")
