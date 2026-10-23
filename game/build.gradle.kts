plugins {
    id("tamias.java-conventions")
    `maven-publish`
    alias(libs.plugins.shadow)
    application
}

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.game.bom))
    implementation(platform(libs.falco.bom))
    implementation(libs.atlas.projectiles)
    implementation(libs.adventure)
    implementation(libs.slf4j.api)

    // CloudNet is provided by the CloudNet wrapper at runtime and its bridge is loaded as a
    // Minestom extension (separate classloader, see the :bridge module), so :setup neither
    // references nor bundles any CloudNet artifact.
    implementation(platform(libs.minestom.extensions.bom))
    implementation(libs.minestom.extensions)

    // LuckPerms
    implementation(libs.guava)
    compileOnly(libs.luckperms.api) {
        exclude(group = "net.kyori.adventure")
    }
    runtimeOnly(libs.luckperms.minestom.loader) {
        exclude(group = "net.kyori.adventure")
    }

    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)
    compileOnly(libs.falco.anvil)
    // SLF4J needs a binding at runtime; without one it falls back to NOP and the
    // server logs nothing at all.
    runtimeOnly(libs.slf4j.simple)

    testImplementation(libs.minestom)
    testImplementation(libs.cyano)
    testImplementation(libs.aves)
    testImplementation(libs.xerus)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.engine)
}

application {
    mainClass.set("net.theevilreaper.tamias.TamiasLauncher")
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("")
    }
}

publishing {
    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            }
            name = "OneLiteFeatherRepository"
            url = if (project.version.toString().contains("SNAPSHOT")) {
                uri("https://repo.onelitefeather.dev/onelitefeather-snapshots")
            } else {
                uri("https://repo.onelitefeather.dev/onelitefeather-releases")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            artifact(project.tasks.getByName("shadowJar"))
            version = rootProject.version as String
            artifactId = "tamias-game"
            groupId = rootProject.group as String
            pom {
                description.set("Game component of the Tamias MiniGame")
                name = "Tamias Game Component"
                url = "https://github.com/OneLiteFeatherNET/Tamias"
                licenses {
                    license {
                        name = "AGPL-3.0 License"
                        url = "https://www.gnu.org/licenses/agpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        name.set("OneliteFeather")
                        contributors {
                            contributor {
                                name.set("theEvilReaper")
                            }
                            contributor {
                                name.set("TheMeinerLP")
                            }
                        }
                    }
                }

                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/OneLiteFeatherNET/Tamias/issues")
                }

                scm {
                    connection = "scm:git:git://github.com:OneLiteFeatherNET/Tamias.git"
                    developerConnection = "scm:git:ssh://git@github.com:OneLiteFeatherNET/Tamias.git"
                    url = "https://github.com/OneLiteFeatherNET/Tamias"
                }
            }
        }
    }
}
