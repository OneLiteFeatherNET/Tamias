rootProject.name = "tamias"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("minestom", "1.3.1")
            version("junit", "5.11.3")
            version("mockito", "5.11.0")
            library("minestom", "net.onelitefeather.microtus", "Minestom").versionRef("minestom")
            library("minestom-test", "net.onelitefeather.microtus.testing", "testing").versionRef("minestom")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            library("mockito-core", "org.mockito", "mockito-core").versionRef("mockito")
            library("mockito-junit", "org.mockito", "mockito-junit-jupiter").versionRef("mockito")
            library("aves", "de.icevizion.lib", "aves").version("1.4.0+6f7eaef2")
            library("xerus", "net.theevilreaper.xerus", "Xerus").version("1.2.0-SNAPSHOT+4eafdb81")
            library("canis", "com.github.theEvilReaper", "Canis").version("master-SNAPSHOT")
        }
    }
}
