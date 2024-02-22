rootProject.name = "tamias"
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("minestom", "1.3.1")
            version("junit", "5.10.2")
            version("mockito", "5.9.0")
            library("minestom", "net.onelitefeather.microtus", "Minestom").versionRef("minestom")
            library("minestom-test", "net.onelitefeather.microtus.testing", "testing").versionRef("minestom")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            library("mockito-core", "org.mockito", "mockito-core").versionRef("mockito")
            library("mockito-junit", "org.mockito", "mockito-junit-jupiter").versionRef("mockito")
            library("aves", "de.icevizion.lib", "Aves").version("1.3.0+f7b17be8")
            library("xerus", "net.theevilreaper.xerus", "Xerus").version("1.2.0-SNAPSHOT+d6f910bf")
            library("canis", "com.github.theEvilReaper", "Canis").version("master-SNAPSHOT")
            library("mini", "net.kyori", "adventure-text-minimessage").version("4.14.0")
        }
    }
}
